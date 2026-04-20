package net.chrisrichardson.ftgo.courierservice.web;

import net.chrisrichardson.ftgo.courierservice.domain.Courier;
import net.chrisrichardson.ftgo.courierservice.domain.CourierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/couriers")
public class CourierController {

    private final CourierRepository courierRepository;

    public CourierController(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    @PostMapping
    public ResponseEntity<Courier> create(@RequestBody Courier courier) {
        Courier saved = courierRepository.save(courier);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{courierId}")
    public ResponseEntity<Courier> get(@PathVariable long courierId) {
        Optional<Courier> courier = courierRepository.findById(courierId);
        return courier.map(c -> new ResponseEntity<>(c, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/available")
    public ResponseEntity<List<Courier>> getAvailable() {
        List<Courier> couriers = courierRepository.findByAvailableTrue();
        return new ResponseEntity<>(couriers, HttpStatus.OK);
    }
}
