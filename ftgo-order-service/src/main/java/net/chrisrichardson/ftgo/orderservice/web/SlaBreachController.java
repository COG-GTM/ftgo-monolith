package net.chrisrichardson.ftgo.orderservice.web;

import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.orderservice.api.web.SlaBreachReport;
import net.chrisrichardson.ftgo.orderservice.domain.SlaBreachService;
import net.chrisrichardson.ftgo.orderservice.domain.TerminalOrderStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/orders/sla-breaches")
public class SlaBreachController {

  private final SlaBreachService slaBreachService;

  public SlaBreachController(SlaBreachService slaBreachService) {
    this.slaBreachService = slaBreachService;
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<SlaBreachReport> getSlaBreaches(@RequestParam(required = false) String state) {
    if (state == null) {
      return new ResponseEntity<>(slaBreachService.findBreaches(), HttpStatus.OK);
    }

    OrderState orderState;
    try {
      orderState = OrderState.valueOf(state.toUpperCase());
    } catch (IllegalArgumentException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    try {
      return new ResponseEntity<>(slaBreachService.findBreaches(orderState), HttpStatus.OK);
    } catch (TerminalOrderStateException e) {
      return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
  }
}
