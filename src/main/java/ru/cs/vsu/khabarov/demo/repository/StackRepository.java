package ru.cs.vsu.khabarov.demo.repository;

import ru.cs.vsu.khabarov.demo.model.Stack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface StackRepository extends JpaRepository<Stack, Long> {

    // Поиск ближайших полок к точке (px, py) в радиусе maxDistance — евклидово расстояние
    @Query(value = """
            SELECT * FROM stack
            WHERE sqrt(power(x - :px, 2) + power(y - :py, 2)) <= :maxDistance
            ORDER BY sqrt(power(x - :px, 2) + power(y - :py, 2))
            """, nativeQuery = true)
    List<Stack> findStacksNear(@Param("px") double px,
                               @Param("py") double py,
                               @Param("maxDistance") double maxDistance);
}
