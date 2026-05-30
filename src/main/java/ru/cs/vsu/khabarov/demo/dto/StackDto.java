package ru.cs.vsu.khabarov.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StackDto {
    @NotNull
    private Double x;

    @NotNull
    private Double y;

    @NotBlank
    private String shelfNumber;

    private String description;
}
