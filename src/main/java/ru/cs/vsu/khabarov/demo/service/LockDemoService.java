package ru.cs.vsu.khabarov.demo.service;

import jakarta.persistence.OptimisticLockException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.cs.vsu.khabarov.demo.model.Book;
import ru.cs.vsu.khabarov.demo.repository.BookRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LockDemoService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    // ── Оптимистичная блокировка (@Version) ──────────────────────────────────

    @Transactional
    public void optimisticUpdate(Long bookId, double newPrice) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Книга не найдена: " + bookId));
        book.setPrice(newPrice);
        book.updateTimestamp();
        bookRepository.save(book);
    }

    public DemoResult runOptimistic(Long bookId, int threadCount) throws InterruptedException {
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger retries = new AtomicInteger(0);
        AtomicInteger failed  = new AtomicInteger(0);
        List<String>  log     = new CopyOnWriteArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final double price = 100.0 + i * 10;
            final int    num   = i + 1;
            futures.add(pool.submit(() -> {
                latch.countDown();
                try { latch.await(); } catch (InterruptedException ignored) {}

                int attempt = 0;
                while (attempt < 5) {
                    try {
                        optimisticUpdate(bookId, price);
                        success.incrementAndGet();
                        log.add("Поток-" + num + ": установил цену " + price + " (попытка " + (attempt + 1) + ")");
                        return;
                    } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                        attempt++;
                        retries.incrementAndGet();
                        log.add("Поток-" + num + ": конфликт версий, retry #" + attempt);
                        try { Thread.sleep(10 * attempt); } catch (InterruptedException ignored) {}
                    }
                }
                failed.incrementAndGet();
                log.add("Поток-" + num + ": исчерпал попытки, пропущен");
            }));
        }

        for (Future<?> f : futures) {
            try { f.get(); } catch (ExecutionException ignored) {}
        }
        pool.shutdown();

        Book finalBook = bookRepository.findById(bookId).orElseThrow();
        return new DemoResult("OPTIMISTIC (@Version)", threadCount,
                success.get(), retries.get(), failed.get(),
                finalBook.getPrice(), finalBook.getVersion(), log);
    }

    // ── Пессимистичная блокировка (SELECT FOR UPDATE) ────────────────────────

    public void pessimisticUpdate(Long bookId, double newPrice) {
        // TransactionTemplate явно открывает транзакцию в текущем потоке,
        // чтобы @Lock(PESSIMISTIC_WRITE) мог удержать блокировку до коммита
        transactionTemplate.execute(status -> {
            Book book = bookRepository.findByIdForUpdate(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Книга не найдена: " + bookId));
            book.setPrice(newPrice);
            book.updateTimestamp();
            bookRepository.save(book);
            return null;
        });
    }

    public DemoResult runPessimistic(Long bookId, int threadCount) throws InterruptedException {
        AtomicInteger success = new AtomicInteger(0);
        List<String>  log     = new CopyOnWriteArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final double price = 100.0 + i * 10;
            final int    num   = i + 1;
            futures.add(pool.submit(() -> {
                latch.countDown();
                try { latch.await(); } catch (InterruptedException ignored) {}
                try {
                    pessimisticUpdate(bookId, price);
                    success.incrementAndGet();
                    log.add("Поток-" + num + ": установил цену " + price + " (ждал блокировку)");
                } catch (Exception e) {
                    log.add("Поток-" + num + ": ошибка — " + e.getMessage());
                }
            }));
        }

        for (Future<?> f : futures) {
            try { f.get(); } catch (ExecutionException ignored) {}
        }
        pool.shutdown();

        Book finalBook = bookRepository.findById(bookId).orElseThrow();
        return new DemoResult("PESSIMISTIC (SELECT FOR UPDATE)", threadCount,
                success.get(), 0, threadCount - success.get(),
                finalBook.getPrice(), finalBook.getVersion(), log);
    }

    // ── DTO результата ────────────────────────────────────────────────────────

    public record DemoResult(
            String strategy,
            int threads,
            int successCount,
            int retryCount,
            int failedCount,
            double finalPrice,
            Long finalVersion,
            List<String> log
    ) {}
}
