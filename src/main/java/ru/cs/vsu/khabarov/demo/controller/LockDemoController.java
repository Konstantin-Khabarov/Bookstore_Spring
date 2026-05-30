package ru.cs.vsu.khabarov.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.cs.vsu.khabarov.demo.service.LockDemoService;
import ru.cs.vsu.khabarov.demo.service.LockDemoService.DemoResult;

@RestController
@RequestMapping("/api/lock-demo")
@Tag(name = "Lock Demo (п.11)", description = "Демонстрация блокировок при конкурентной записи из нескольких источников")
public class LockDemoController {

    @Autowired
    private LockDemoService lockDemoService;

    @PostMapping("/optimistic")
    @Operation(
        summary = "Оптимистичная блокировка (@Version)",
        description = "Запускает N потоков, которые одновременно обновляют одну книгу. " +
                      "При конфликте версий JPA бросает OptimisticLockException — поток делает retry. " +
                      "Все обновления в итоге применяются."
    )
    public DemoResult optimistic(
            @RequestParam(defaultValue = "1") Long bookId,
            @RequestParam(defaultValue = "5") int threads) throws InterruptedException {
        return lockDemoService.runOptimistic(bookId, threads);
    }

    @PostMapping("/pessimistic")
    @Operation(
        summary = "Пессимистичная блокировка (SELECT FOR UPDATE)",
        description = "Запускает N потоков одновременно. Первый захватывает строку через SELECT FOR UPDATE, " +
                      "остальные ждут в очереди. Никаких конфликтов — потоки выполняются строго последовательно."
    )
    public DemoResult pessimistic(
            @RequestParam(defaultValue = "1") Long bookId,
            @RequestParam(defaultValue = "5") int threads) throws InterruptedException {
        return lockDemoService.runPessimistic(bookId, threads);
    }
}
