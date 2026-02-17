package net.chrisrichardson.ftgo.consumerservice.web;

import net.chrisrichardson.ftgo.consumerservice.domain.Consumer;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/consumers")
public class ConsumerController {

    private final ConsumerRepository consumerRepository;

    public ConsumerController(ConsumerRepository consumerRepository) {
        this.consumerRepository = consumerRepository;
    }

    @PostMapping
    public ResponseEntity<Consumer> create(@RequestBody Consumer consumer) {
        Consumer saved = consumerRepository.save(consumer);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{consumerId}")
    public ResponseEntity<Consumer> get(@PathVariable long consumerId) {
        Optional<Consumer> consumer = consumerRepository.findById(consumerId);
        return consumer.map(c -> new ResponseEntity<>(c, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
