package ru.cs.vsu.khabarov.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.cs.vsu.khabarov.demo.kafka.BookEvent;
import ru.cs.vsu.khabarov.demo.model.BookEventLog;
import ru.cs.vsu.khabarov.demo.repository.BookEventLogRepository;

import java.util.List;

@RestController
@RequestMapping("/api/event-log")
@Tag(name = "Event Log (п.10)", description = "Журнал событий, записанных Kafka-consumer-ом в БД")
public class BookEventLogController {

    @Autowired
    private BookEventLogRepository eventLogRepository;

    @GetMapping
    @Operation(summary = "Все события из book_event_log")
    public List<BookEventLog> getAll() {
        return eventLogRepository.findAll();
    }

    @GetMapping("/book/{bookId}")
    @Operation(summary = "События по конкретной книге")
    public List<BookEventLog> getByBook(@PathVariable Long bookId) {
        return eventLogRepository.findByBookId(bookId);
    }

    @GetMapping("/type/{eventType}")
    @Operation(summary = "События по типу: CREATED, UPDATED, DELETED")
    public List<BookEventLog> getByType(@PathVariable BookEvent.Type eventType) {
        return eventLogRepository.findByEventType(eventType);
    }
}
