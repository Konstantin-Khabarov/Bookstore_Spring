package ru.cs.vsu.khabarov.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stack", indexes = {
    @Index(name = "idx_stack_location", columnList = "x, y")
})
public class Stack {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double x;

    @Column(nullable = false)
    private Double y;

    private String shelfNumber;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Stack() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
