package ru.cs.vsu.khabarov.demo.loader;

import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Запускать как отдельный main-класс в IntelliJ.
 * Приложение должно быть запущено на localhost:8081.
 */
public class DataLoader {

    static final String BASE_URL   = "http://localhost:8081";
    static final int BATCH_SIZE    = 500;
    static final int TOTAL_RECORDS = 100000;

    static final String[] AUTHORS = {
        "Лев Толстой", "Фёдор Достоевский", "Антон Чехов", "Иван Тургенев",
        "Николай Гоголь", "Александр Пушкин", "Михаил Булгаков", "Иван Бунин",
        "Борис Пастернак", "Михаил Лермонтов"
    };
    static final String[] TITLES = {
        "Война и мир", "Преступление и наказание", "Вишнёвый сад", "Отцы и дети",
        "Мёртвые души", "Евгений Онегин", "Мастер и Маргарита", "Тёмные аллеи",
        "Доктор Живаго", "Герой нашего времени"
    };

    public static void main(String[] args) {
        // RestClient с базовым URL — Jackson подхватывается автоматически
        RestClient restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .build();

        System.out.println("=== Пункт 6: Заполнение БД через REST (Spring RestClient) ===\n");

        // Однопоточная загрузка
        System.out.println("--- Однопоточная загрузка (" + TOTAL_RECORDS + " записей) ---");
        long t1 = System.currentTimeMillis();
        loadSingleThreaded(restClient, TOTAL_RECORDS, BATCH_SIZE);
        long t2 = System.currentTimeMillis();
        long singleTime = t2 - t1;
        System.out.printf("Время: %d мс | Скорость: %.0f записей/сек%n%n",
                singleTime, TOTAL_RECORDS * 1000.0 / singleTime);

        // Многопоточная загрузка
        int threads = Runtime.getRuntime().availableProcessors();
        System.out.println("--- Многопоточная загрузка (" + TOTAL_RECORDS + " записей, " + threads + " потоков) ---");
        long t3 = System.currentTimeMillis();
        loadMultiThreaded(TOTAL_RECORDS, BATCH_SIZE, threads);
        long t4 = System.currentTimeMillis();
        long multiTime = t4 - t3;
        System.out.printf("Время: %d мс | Скорость: %.0f записей/сек%n", multiTime, TOTAL_RECORDS * 1000.0 / multiTime);
        System.out.printf("Ускорение: %.1fx%n", (double) singleTime / multiTime);
    }

    // ── Однопоточная загрузка ──────────────────────────────────────────────

    static void loadSingleThreaded(RestClient restClient, int total, int batchSize) {
        Random rnd = new Random(42);
        int batches = total / batchSize;

        for (int i = 0; i < batches; i++) {
            List<Map<String, Object>> batch = generateBatch(i * batchSize, batchSize, rnd);
            sendBatch(restClient, batch);
            if ((i + 1) % 10 == 0) {
                System.out.printf("  Отправлено: %d / %d%n", (i + 1) * batchSize, total);
            }
        }
    }

    // ── Многопоточная загрузка ─────────────────────────────────────────────

    static void loadMultiThreaded(int total, int batchSize, int threadCount) {
        int batches = total / batchSize;
        AtomicInteger done = new AtomicInteger(0);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < batches; i++) {
            final int batchIndex = i;
            futures.add(pool.submit(() -> {
                // каждый поток создаёт свой RestClient
                RestClient client = RestClient.builder().baseUrl(BASE_URL).build();
                List<Map<String, Object>> batch = generateBatch(batchIndex * batchSize, batchSize, new Random());
                sendBatch(client, batch);
                int n = done.addAndGet(batchSize);
                if (n % (batchSize * 10) == 0) {
                    System.out.printf("  Отправлено: %d / %d%n", n, total);
                }
            }));
        }

        try {
            for (Future<?> f : futures) f.get();
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
        pool.shutdown();
    }

    // ── Генерация батча ────────────────────────────────────────────────────

    static List<Map<String, Object>> generateBatch(int offset, int size, Random rnd) {
        List<Map<String, Object>> batch = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int idx = offset + i;
            Map<String, Object> book = new HashMap<>();
            book.put("title",       TITLES[idx % TITLES.length] + " #" + idx);
            book.put("author",      AUTHORS[idx % AUTHORS.length]);
            book.put("description", "Описание книги номер " + idx + ". Жанр: " +
                    (idx % 3 == 0 ? "роман" : idx % 3 == 1 ? "повесть" : "рассказ") + ".");
            book.put("price",       100 + rnd.nextInt(1900));
            book.put("categoryId",  (long)(idx % 3) + 1);
            book.put("stackId",     (long)(idx % 3) + 1);
            batch.add(book);
        }
        return batch;
    }

    // ── Отправка батча через RestClient ───────────────────────────────────

    static void sendBatch(RestClient restClient, List<Map<String, Object>> batch) {
        restClient.post()
                .uri("/api/books/bulk")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(batch)           // Jackson сериализует автоматически
                .retrieve()
                .toBodilessEntity();   // нам ответ не нужен, только статус
    }
}
