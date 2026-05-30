package ru.cs.vsu.khabarov.demo.loader;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
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

    static final String URL    = "http://localhost:8081/api/books/bulk";
    static final int BATCH_SIZE    = 500;
    static final int TOTAL_RECORDS = 500_000; // измени на 1_000_000 для 1 млн

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

    static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        System.out.println("=== Пункт 6: Заполнение БД через REST ===\n");

        // Однопоточная загрузка
        System.out.println("--- Однопоточная загрузка (" + TOTAL_RECORDS + " записей) ---");
        long t1 = System.currentTimeMillis();
        loadSingleThreaded(TOTAL_RECORDS, BATCH_SIZE);
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

    static void loadSingleThreaded(int total, int batchSize) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        Random rnd = new Random(42);
        int batches = total / batchSize;

        for (int i = 0; i < batches; i++) {
            String json = generateBatchJson(i * batchSize, batchSize, rnd);
            sendBatch(client, json);
            if ((i + 1) % 10 == 0) {
                System.out.printf("  Отправлено: %d / %d%n", (i + 1) * batchSize, total);
            }
        }
    }

    // ── Многопоточная загрузка ─────────────────────────────────────────────

    static void loadMultiThreaded(int total, int batchSize, int threadCount) throws Exception {
        int batches = total / batchSize;
        AtomicInteger done = new AtomicInteger(0);
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < batches; i++) {
            final int batchIndex = i;
            futures.add(pool.submit(() -> {
                try {
                    HttpClient client = HttpClient.newHttpClient();
                    String json = generateBatchJson(batchIndex * batchSize, batchSize, new Random());
                    sendBatch(client, json);
                    int n = done.addAndGet(batchSize);
                    if (n % (batchSize * 10) == 0) {
                        System.out.printf("  Отправлено: %d / %d%n", n, total);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка в батче " + batchIndex + ": " + e.getMessage());
                }
            }));
        }

        for (Future<?> f : futures) f.get();
        pool.shutdown();
    }

    // ── Генерация JSON через Jackson (корректное экранирование) ────────────

    static String generateBatchJson(int offset, int size, Random rnd) throws Exception {
        List<Map<String, Object>> batch = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            int idx = offset + i;
            Map<String, Object> book = new HashMap<>();
            book.put("title",       TITLES[idx % TITLES.length] + " #" + idx);
            book.put("author",      AUTHORS[idx % AUTHORS.length]);
            book.put("description", "Описание книги номер " + idx + ". Жанр: " + (idx % 3 == 0 ? "роман" : idx % 3 == 1 ? "повесть" : "рассказ") + ".");
            book.put("price",       100 + rnd.nextInt(1900));
            book.put("categoryId",  (long)(idx % 3) + 1);
            book.put("stackId",     (long)(idx % 3) + 1);
            batch.add(book);
        }
        return mapper.writeValueAsString(batch);
    }

    // ── Отправка батча ─────────────────────────────────────────────────────

    static void sendBatch(HttpClient client, String json) throws Exception {
        byte[] body = json.getBytes(StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(URL))
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + ": " + response.body());
        }
    }
}
