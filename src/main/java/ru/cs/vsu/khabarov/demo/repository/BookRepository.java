package ru.cs.vsu.khabarov.demo.repository;

import ru.cs.vsu.khabarov.demo.model.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

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

    // Пессимистичная блокировка: SELECT ... FOR UPDATE (п.11)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.id = :id")
    Optional<Book> findByIdForUpdate(@Param("id") Long id);

    // Выборка с сортировкой по названию (п.7)
    @Query(value = "SELECT * FROM book ORDER BY title LIMIT :limit", nativeQuery = true)
    List<Book> findAllOrderByTitle(@Param("limit") int limit);

    // JOIN с таблицей category (п.7)
    @Query(value = """
            SELECT b.id AS bookId, b.title, b.author, b.price, c.name AS categoryName
            FROM book b
            JOIN category c ON b.category_id = c.id
            WHERE b.category_id = :categoryId
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findBooksWithCategoryRaw(@Param("categoryId") Long categoryId,
                                            @Param("limit") int limit);

    // Агрегация: статистика по категориям (п.7)
    @Query(value = """
            SELECT b.category_id        AS categoryId,
                   COUNT(*)             AS bookCount,
                   AVG(b.price)         AS avgPrice,
                   MIN(b.price)         AS minPrice,
                   MAX(b.price)         AS maxPrice
            FROM book b
            GROUP BY b.category_id
            ORDER BY bookCount DESC
            """, nativeQuery = true)
    List<Object[]> categoryStatsRaw();
}
