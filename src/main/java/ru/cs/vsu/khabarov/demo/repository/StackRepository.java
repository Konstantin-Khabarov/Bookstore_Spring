package ru.cs.vsu.khabarov.demo.repository;

import ru.cs.vsu.khabarov.demo.model.Stack;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface StackRepository extends MongoRepository<Stack, String> {
    // Поиск ближайших полок к точке (x, y) — синтаксис для 2d индекса (плоские координаты)
    @Query("{ 'location': { '$near': [?0, ?1], '$maxDistance': ?2 } }")
    List<Stack> findStacksNear(double x, double y, double maxDistance);
}