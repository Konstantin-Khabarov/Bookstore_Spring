package ru.cs.vsu.khabarov.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryStatsDto {
    private Long categoryId;
    private Long bookCount;
    private Double avgPrice;
    private Double minPrice;
    private Double maxPrice;
}
