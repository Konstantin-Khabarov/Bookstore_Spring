package ru.cs.vsu.khabarov.demo.repository;

import ru.cs.vsu.khabarov.demo.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // Полнотекстовый поиск через PostgreSQL tsvector (использует GIN-индекс)
    @Query(value = """
            SELECT * FROM book
            WHERE to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(description,''))
                  @@ plainto_tsquery('simple', :keyword)
            """, nativeQuery = true)
    List<Book> fullTextSearch(@Param("keyword") String keyword);

    List<Book> findByAuthor(String author);

    List<Book> findByCategoryId(Long categoryId);

    List<Book> findByStackId(Long stackId);

    List<Book> findByPriceBetween(Double minPrice, Double maxPrice);
}
