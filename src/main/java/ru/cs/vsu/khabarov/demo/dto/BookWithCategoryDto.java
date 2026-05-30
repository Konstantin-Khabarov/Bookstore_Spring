package ru.cs.vsu.khabarov.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookWithCategoryDto {
    private Long bookId;
    private String title;
    private String author;
    private Double price;
    private String categoryName;
}
