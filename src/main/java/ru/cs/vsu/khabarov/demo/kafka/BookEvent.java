package ru.cs.vsu.khabarov.demo.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookEvent {

    public enum Type { CREATED, UPDATED, DELETED }

    private Type eventType;
    private Long bookId;
    private String title;
    private String author;
    private Double price;
    private Long categoryId;
    private LocalDateTime occurredAt;
}
