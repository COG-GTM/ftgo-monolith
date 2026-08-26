package net.chrisrichardson.ftgo.orderservice.api.web;

import java.util.List;
import java.util.Map;

public class SlaBreachReport {

  private int totalBreaches;
  private Map<String, Integer> breachCountsByState;
  private Map<String, Integer> thresholdMinutesByState;
  private List<SlaBreachedOrder> breaches;

  private SlaBreachReport() {
  }

  public SlaBreachReport(Map<String, Integer> breachCountsByState,
                         Map<String, Integer> thresholdMinutesByState,
                         List<SlaBreachedOrder> breaches) {
    this.breachCountsByState = breachCountsByState;
    this.thresholdMinutesByState = thresholdMinutesByState;
    this.breaches = breaches;
    this.totalBreaches = breaches.size();
  }

  public int getTotalBreaches() {
    return totalBreaches;
  }

  public Map<String, Integer> getBreachCountsByState() {
    return breachCountsByState;
  }

  public Map<String, Integer> getThresholdMinutesByState() {
    return thresholdMinutesByState;
  }

  public List<SlaBreachedOrder> getBreaches() {
    return breaches;
  }
}
