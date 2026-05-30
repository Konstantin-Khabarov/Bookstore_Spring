package ru.cs.vsu.khabarov.demo.repository;

import ru.cs.vsu.khabarov.demo.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookRepository extends MongoRepository<Book, String> {

    @Query("{ $text: { $search: ?0 } }")
    List<Book> fullTextSearch(String keyword);

    List<Book> findByTitle(String title);

    List<Book> findByAuthor(String author);

    List<Book> findByAuthorAndTitle(String author, String title);

    // Поиск книг по жанру
    @Query("{ 'genres' : ?0 }")
    List<Book> findByGenre(String genre);

    List<Book> findByCategoryId(String categoryId);

    List<Book> findByStackId(String stackId);

    List<Book> findByPriceBetween(Double minPrice, Double maxPrice);

}
