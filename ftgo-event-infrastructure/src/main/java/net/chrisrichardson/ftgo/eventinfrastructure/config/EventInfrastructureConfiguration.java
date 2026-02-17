package net.chrisrichardson.ftgo.eventinfrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.chrisrichardson.ftgo.eventinfrastructure.domain.DomainEventPublisher;
import net.chrisrichardson.ftgo.eventinfrastructure.kafka.KafkaEventPublisher;
import net.chrisrichardson.ftgo.eventinfrastructure.outbox.OutboxEventPublisher;
import net.chrisrichardson.ftgo.eventinfrastructure.outbox.OutboxRelay;
import net.chrisrichardson.ftgo.eventinfrastructure.outbox.OutboxRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableScheduling
@EnableJpaRepositories(basePackageClasses = OutboxRepository.class)
@EntityScan(basePackages = "net.chrisrichardson.ftgo.eventinfrastructure.outbox")
public class EventInfrastructureConfiguration {

    @Value("${ftgo.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.RETRIES_CONFIG, 3);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaEventPublisher kafkaEventPublisher(KafkaTemplate<String, String> kafkaTemplate) {
        return new KafkaEventPublisher(kafkaTemplate);
    }

    @Bean
    public ObjectMapper eventObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public DomainEventPublisher domainEventPublisher(OutboxRepository outboxRepository, ObjectMapper eventObjectMapper) {
        return new OutboxEventPublisher(outboxRepository, eventObjectMapper);
    }

    @Bean
    public OutboxRelay outboxRelay(OutboxRepository outboxRepository, KafkaEventPublisher kafkaEventPublisher) {
        return new OutboxRelay(outboxRepository, kafkaEventPublisher);
    }
}
