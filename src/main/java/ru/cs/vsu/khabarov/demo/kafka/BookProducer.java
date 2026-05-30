package ru.cs.vsu.khabarov.demo.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import ru.cs.vsu.khabarov.demo.model.Book;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class BookProducer {

    static final String TOPIC = "books-topic";

    @Autowired
    private KafkaTemplate<String, BookEvent> kafkaTemplate;

    public void sendCreated(Book book) {
        send(BookEvent.Type.CREATED, book);
    }

    public void sendUpdated(Book book) {
        send(BookEvent.Type.UPDATED, book);
    }

    public void sendDeleted(Long bookId) {
        BookEvent event = new BookEvent(BookEvent.Type.DELETED, bookId,
                null, null, null, null, LocalDateTime.now());
        dispatch(event);
    }

    private void send(BookEvent.Type type, Book book) {
        BookEvent event = new BookEvent(
                type,
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getCategoryId(),
                LocalDateTime.now()
        );
        dispatch(event);
    }

    private void dispatch(BookEvent event) {
        // одна книга всегда попадает в одну партицию — удобно для упорядоченной обработки
        String key = String.valueOf(event.getBookId());
        CompletableFuture<SendResult<String, BookEvent>> future =
                kafkaTemplate.send(TOPIC, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.err.printf("[Kafka] Ошибка отправки события %s для bookId=%s: %s%n",
                        event.getEventType(), event.getBookId(), ex.getMessage());
            } else {
                System.out.printf("[Kafka] Отправлено: %s bookId=%s → partition=%d offset=%d%n",
                        event.getEventType(), event.getBookId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
