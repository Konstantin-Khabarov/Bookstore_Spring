package ru.cs.vsu.khabarov.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.cs.vsu.khabarov.demo.kafka.BookEvent;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "book_event_log")
public class BookEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookEvent.Type eventType;

    private Long bookId;
    private String title;
    private String author;
    private Double price;
    private Long categoryId;

    private LocalDateTime occurredAt;
    private LocalDateTime receivedAt;

    public static BookEventLog from(BookEvent event) {
        BookEventLog log = new BookEventLog();
        log.setEventType(event.getEventType());
        log.setBookId(event.getBookId());
        log.setTitle(event.getTitle());
        log.setAuthor(event.getAuthor());
        log.setPrice(event.getPrice());
        log.setCategoryId(event.getCategoryId());
        log.setOccurredAt(event.getOccurredAt());
        log.setReceivedAt(LocalDateTime.now());
        return log;
    }
}
