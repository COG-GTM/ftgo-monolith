package net.chrisrichardson.ftgo.eventinfrastructure.outbox;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OutboxRepository extends CrudRepository<OutboxEvent, Long> {

    @Query("SELECT e FROM OutboxEvent e WHERE e.published = false ORDER BY e.createdAt ASC")
    List<OutboxEvent> findUnpublishedEvents();
}
