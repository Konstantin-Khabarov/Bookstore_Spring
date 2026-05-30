package ru.cs.vsu.khabarov.demo.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class BookDto {
    @NotBlank(message = "Название книги обязательно")
    @Size(min = 1, max = 200, message = "Название должно быть от 1 до 200 символов")
    private String title;

    @NotBlank(message = "Автор обязателен")
    @Size(min = 2, max = 200, message = "Имя автора должно быть от 2 до 200 символов")
    private String author;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Цена должна быть больше 0")
    private Double price;

    //private List<String> genres;

    @NotBlank
    private String categoryId;   // ссылка на категорию

    @NotBlank
    private String stackId;

}
