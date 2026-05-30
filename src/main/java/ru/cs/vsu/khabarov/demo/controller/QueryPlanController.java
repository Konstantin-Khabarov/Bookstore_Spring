package ru.cs.vsu.khabarov.demo.controller;

import ru.cs.vsu.khabarov.demo.dto.BookWithCategoryDto;
import ru.cs.vsu.khabarov.demo.dto.CategoryStatsDto;
import ru.cs.vsu.khabarov.demo.model.Book;
import ru.cs.vsu.khabarov.demo.repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/query-plan")
@Tag(name = "Query Plan (п.7)", description = "Демонстрация EXPLAIN ANALYZE для 5 типов запросов")
public class QueryPlanController {

    @Autowired
    private BookRepository bookRepository;

    @PersistenceContext
    private EntityManager em;

    // ── 1. Выборка по id ──────────────────────────────────────────────────────

    @GetMapping("/by-id")
    @Operation(summary = "Выборка по id — данные")
    public ResponseEntity<Book> byId(@RequestParam(defaultValue = "1") Long id) {
        Optional<Book> book = bookRepository.findById(id);
        return book.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-id/explain")
    @Operation(summary = "Выборка по id — EXPLAIN ANALYZE",
               description = "SELECT * FROM book WHERE id = :id")
    public List<String> byIdExplain(@RequestParam(defaultValue = "1") Long id) {
        return explain("SELECT * FROM book WHERE id = " + id);
    }

    // ── 2. Выборка по полю (category_id) ─────────────────────────────────────

    @GetMapping("/by-field")
    @Operation(summary = "Выборка по полю category_id — данные (первые 20)")
    public List<Book> byField(@RequestParam(defaultValue = "1") Long categoryId) {
        return bookRepository.findByCategoryId(categoryId).stream().limit(20).toList();
    }

    @GetMapping("/by-field/explain")
    @Operation(summary = "Выборка по полю category_id — EXPLAIN ANALYZE",
               description = "SELECT * FROM book WHERE category_id = :categoryId")
    public List<String> byFieldExplain(@RequestParam(defaultValue = "1") Long categoryId) {
        return explain("SELECT * FROM book WHERE category_id = " + categoryId);
    }

    // ── 3. Выборка с сортировкой ──────────────────────────────────────────────

    @GetMapping("/sorted")
    @Operation(summary = "Выборка с сортировкой по title — данные")
    public List<Book> sorted(@RequestParam(defaultValue = "20") int limit) {
        return bookRepository.findAllOrderByTitle(limit);
    }

    @GetMapping("/sorted/explain")
    @Operation(summary = "Выборка с сортировкой по title — EXPLAIN ANALYZE",
               description = "SELECT * FROM book ORDER BY title LIMIT :limit")
    public List<String> sortedExplain(@RequestParam(defaultValue = "20") int limit) {
        return explain("SELECT * FROM book ORDER BY title LIMIT " + limit);
    }

    // ── 4. Выборка с JOIN ─────────────────────────────────────────────────────

    @GetMapping("/join")
    @Operation(summary = "Выборка с JOIN (book + category) — данные")
    public List<BookWithCategoryDto> join(@RequestParam(defaultValue = "1") Long categoryId,
                                          @RequestParam(defaultValue = "20") int limit) {
        return bookRepository.findBooksWithCategoryRaw(categoryId, limit).stream()
                .map(row -> new BookWithCategoryDto(
                        toLong(row[0]),
                        (String) row[1],
                        (String) row[2],
                        toDouble(row[3]),
                        (String) row[4]))
                .toList();
    }

    @GetMapping("/join/explain")
    @Operation(summary = "Выборка с JOIN — EXPLAIN ANALYZE",
               description = "SELECT b.*, c.name FROM book b JOIN category c ON b.category_id = c.id WHERE b.category_id = :categoryId")
    public List<String> joinExplain(@RequestParam(defaultValue = "1") Long categoryId) {
        return explain("""
                SELECT b.id, b.title, b.author, b.price, c.name AS category_name
                FROM book b
                JOIN category c ON b.category_id = c.id
                WHERE b.category_id = %d
                """.formatted(categoryId));
    }

    // ── 5. Выборка с агрегацией ───────────────────────────────────────────────

    @GetMapping("/aggregate")
    @Operation(summary = "Агрегация: статистика по категориям — данные")
    public List<CategoryStatsDto> aggregate() {
        return bookRepository.categoryStatsRaw().stream()
                .map(row -> new CategoryStatsDto(
                        toLong(row[0]),
                        toLong(row[1]),
                        toDouble(row[2]),
                        toDouble(row[3]),
                        toDouble(row[4])))
                .toList();
    }

    @GetMapping("/aggregate/explain")
    @Operation(summary = "Агрегация: статистика по категориям — EXPLAIN ANALYZE",
               description = "SELECT category_id, COUNT(*), AVG(price), MIN(price), MAX(price) FROM book GROUP BY category_id")
    public List<String> aggregateExplain() {
        return explain("""
                SELECT category_id, COUNT(*) AS book_count,
                       AVG(price) AS avg_price,
                       MIN(price) AS min_price,
                       MAX(price) AS max_price
                FROM book
                GROUP BY category_id
                ORDER BY book_count DESC
                """);
    }

    // ── Вспомогательный метод: запуск EXPLAIN ANALYZE ─────────────────────────

    @SuppressWarnings("unchecked")
    private List<String> explain(String sql) {
        return (List<String>) em.createNativeQuery(
                "EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT) " + sql
        ).getResultList();
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    private Double toDouble(Object val) {
        if (val == null) return null;
        if (val instanceof Number n) return n.doubleValue();
        if (val instanceof BigDecimal bd) return bd.doubleValue();
        return Double.parseDouble(val.toString());
    }
}
