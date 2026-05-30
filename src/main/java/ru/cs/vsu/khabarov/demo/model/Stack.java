package ru.cs.vsu.khabarov.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "stacks")
public class Stack {
    @Id
    private String id;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2D)
    private double[] location; // [x, y]
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