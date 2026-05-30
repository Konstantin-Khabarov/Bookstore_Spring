package ru.cs.vsu.khabarov.demo.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import ru.cs.vsu.khabarov.demo.model.BookEventLog;
import ru.cs.vsu.khabarov.demo.repository.BookEventLogRepository;

@Service
public class BookConsumer {

    @Autowired
    private BookEventLogRepository eventLogRepository;

    @KafkaListener(
            topics = "books-topic",
            groupId = "bookstore-consumer-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload BookEvent event,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        try {
            BookEventLog log = BookEventLog.from(event);
            eventLogRepository.save(log);
            System.out.printf("[Consumer] Получено: %s bookId=%s | partition=%d offset=%d -> сохранено в book_event_log (id=%d)%n",
                    event.getEventType(), event.getBookId(), partition, offset, log.getId());
        } catch (Exception e) {
            System.err.printf("[Consumer] ОШИБКА сохранения события %s bookId=%s: %s%n",
                    event.getEventType(), event.getBookId(), e.getMessage());
            e.printStackTrace();
        }
    }
}
