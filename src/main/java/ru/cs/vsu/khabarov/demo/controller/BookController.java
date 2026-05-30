package ru.cs.vsu.khabarov.demo.controller;

import ru.cs.vsu.khabarov.demo.dto.BookDto;
import ru.cs.vsu.khabarov.demo.kafka.BookProducer;
import ru.cs.vsu.khabarov.demo.model.Book;
import ru.cs.vsu.khabarov.demo.repository.BookRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Управление книгами и полнотекстовый поиск")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookProducer bookProducer;

    @PostMapping
    @Operation(summary = "Добавить книгу")
    public ResponseEntity<Book> create(@Valid @RequestBody BookDto dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setDescription(dto.getDescription());
        book.setPrice(dto.getPrice());
        book.setCategoryId(dto.getCategoryId());
        book.setStackId(dto.getStackId());
        Book saved = bookRepository.save(book);
        bookProducer.sendCreated(saved);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Получить все книги")
    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получить книгу по ID")
    public ResponseEntity<Book> getById(@PathVariable Long id) {
        Optional<Book> book = bookRepository.findById(id);
        return book.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @Operation(summary = "Полнотекстовый поиск по книгам")
    public List<Book> search(@RequestParam String keyword) {
        return bookRepository.fullTextSearch(keyword);
    }

    @GetMapping("/author/{author}")
    @Operation(summary = "Поиск по автору")
    public List<Book> findByAuthor(@PathVariable String author) {
        return bookRepository.findByAuthor(author);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Книги по категории")
    public List<Book> findByCategory(@PathVariable Long categoryId) {
        return bookRepository.findByCategoryId(categoryId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновить книгу")
    public ResponseEntity<Book> update(@PathVariable Long id, @Valid @RequestBody BookDto dto) {
        Optional<Book> existing = bookRepository.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();
        Book book = existing.get();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setDescription(dto.getDescription());
        book.setPrice(dto.getPrice());
        book.setCategoryId(dto.getCategoryId());
        book.setStackId(dto.getStackId());
        book.updateTimestamp();
        Book saved = bookRepository.save(book);
        bookProducer.sendUpdated(saved);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удалить книгу")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) return ResponseEntity.notFound().build();
        bookRepository.deleteById(id);
        bookProducer.sendDeleted(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    @Operation(summary = "Массовая вставка книг (до 1000 за запрос)")
    public ResponseEntity<Map<String, Integer>> createBulk(@RequestBody List<BookDto> dtos) {
        List<Book> books = dtos.stream().map(dto -> {
            Book book = new Book();
            book.setTitle(dto.getTitle());
            book.setAuthor(dto.getAuthor());
            book.setDescription(dto.getDescription());
            book.setPrice(dto.getPrice());
            book.setCategoryId(dto.getCategoryId());
            book.setStackId(dto.getStackId());
            return book;
        }).toList();
        bookRepository.saveAll(books);
        return ResponseEntity.ok(Map.of("inserted", books.size()));
    }
}
