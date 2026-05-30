package ru.cs.vsu.khabarov.demo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BookDto {
    @NotBlank(message = "Название книги обязательно")
    @Size(min = 1, max = 200)
    private String title;

    @NotBlank(message = "Автор обязателен")
    @Size(min = 2, max = 200)
    private String author;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
    private Double price;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long stackId;
}
