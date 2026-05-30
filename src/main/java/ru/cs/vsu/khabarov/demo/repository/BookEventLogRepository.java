package ru.cs.vsu.khabarov.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.cs.vsu.khabarov.demo.kafka.BookEvent;
import ru.cs.vsu.khabarov.demo.model.BookEventLog;

import java.util.List;

@Repository
public interface BookEventLogRepository extends JpaRepository<BookEventLog, Long> {

    List<BookEventLog> findByBookId(Long bookId);

    List<BookEventLog> findByEventType(BookEvent.Type eventType);
}
