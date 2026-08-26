package net.chrisrichardson.ftgo.orderservice.domain;

import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.OrderRepository;
import net.chrisrichardson.ftgo.domain.OrderState;
import net.chrisrichardson.ftgo.orderservice.api.web.SlaBreachReport;
import net.chrisrichardson.ftgo.orderservice.api.web.SlaBreachedOrder;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static net.chrisrichardson.ftgo.orderservice.OrderDetailsMother.makeOrderInState;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlaBreachServiceTest {

  private static final ZoneId ZONE = ZoneId.systemDefault();

  private OrderRepository orderRepository;
  private SlaProperties slaProperties;
  private LocalDateTime baseTime;

  @Before
  public void setUp() {
    orderRepository = mock(OrderRepository.class);
    slaProperties = new SlaProperties();
    slaProperties.setDefaultThresholdMinutes(30);
    baseTime = LocalDateTime.now();
  }

  private SlaBreachService serviceAt(long minutesAfterBase) {
    Clock clock = Clock.fixed(baseTime.plusMinutes(minutesAfterBase).atZone(ZONE).toInstant(), ZONE);
    return new SlaBreachService(orderRepository, slaProperties, clock);
  }

  private void repositoryContains(Order... orders) {
    List<Order> all = Arrays.asList(orders);
    when(orderRepository.findAllByOrderStateIn(any(Collection.class))).thenAnswer(invocation -> {
      Collection<OrderState> states = (Collection<OrderState>) invocation.getArguments()[0];
      return all.stream().filter(o -> states.contains(o.getOrderState())).collect(toList());
    });
  }

  @Test
  public void shouldFlagOrderOlderThanThreshold() {
    Order order = makeOrderInState(1L, OrderState.PREPARING);
    repositoryContains(order);

    SlaBreachReport report = serviceAt(45).findBreaches();

    assertEquals(1, report.getTotalBreaches());
    SlaBreachedOrder breach = report.getBreaches().get(0);
    assertEquals(Long.valueOf(1L), breach.getOrderId());
    assertEquals(OrderState.PREPARING.name(), breach.getState());
    assertEquals(45, breach.getAgeMinutes());
    assertEquals(30, breach.getThresholdMinutes());
    assertEquals(15, breach.getMinutesOverSla());
    assertEquals(Integer.valueOf(1), report.getBreachCountsByState().get(OrderState.PREPARING.name()));
  }

  @Test
  public void shouldNotFlagOrderAtExactlyTheThreshold() {
    repositoryContains(makeOrderInState(1L, OrderState.PREPARING));

    SlaBreachReport report = serviceAt(30).findBreaches();

    assertEquals(0, report.getTotalBreaches());
    assertEquals(emptyList(), report.getBreaches());
  }

  @Test
  public void shouldPreferPerStateThresholdOverDefault() {
    slaProperties.setStateThresholdMinutes(Collections.singletonMap(OrderState.APPROVED, 10));
    repositoryContains(makeOrderInState(1L, OrderState.APPROVED), makeOrderInState(2L, OrderState.PREPARING));

    SlaBreachReport report = serviceAt(20).findBreaches();

    assertEquals(1, report.getTotalBreaches());
    assertEquals(Long.valueOf(1L), report.getBreaches().get(0).getOrderId());
    assertEquals(10, report.getBreaches().get(0).getThresholdMinutes());
    assertEquals(Integer.valueOf(10), report.getThresholdMinutesByState().get(OrderState.APPROVED.name()));
    assertEquals(Integer.valueOf(30), report.getThresholdMinutesByState().get(OrderState.PREPARING.name()));
  }

  @Test
  public void shouldExcludeTerminalOrders() {
    repositoryContains(makeOrderInState(1L, OrderState.DELIVERED), makeOrderInState(2L, OrderState.CANCELLED));

    SlaBreachReport report = serviceAt(500).findBreaches();

    assertEquals(0, report.getTotalBreaches());
    assertTrue(report.getBreachCountsByState().keySet().stream().noneMatch(s ->
            s.equals(OrderState.DELIVERED.name()) || s.equals(OrderState.CANCELLED.name())));
  }

  @Test
  public void shouldZeroFillCountsForEveryNonTerminalState() {
    repositoryContains();

    SlaBreachReport report = serviceAt(500).findBreaches();

    assertEquals(0, report.getTotalBreaches());
    assertEquals(OrderState.nonTerminalStates().stream().map(Enum::name).collect(toList()),
            report.getBreachCountsByState().keySet().stream().collect(toList()));
    assertTrue(report.getBreachCountsByState().values().stream().allMatch(c -> c == 0));
  }

  @Test
  public void shouldSortBreachesByAgeDescending() {
    Order older = makeOrderInState(1L, OrderState.APPROVED);
    Order newer = makeOrderInState(2L, OrderState.APPROVED);
    slaProperties.setStateThresholdMinutes(Collections.singletonMap(OrderState.APPROVED, 1));
    repositoryContains(older, newer);

    Clock clock = Clock.fixed(baseTime.plusMinutes(60).atZone(ZONE).toInstant(), ZONE);
    SlaBreachReport report = new SlaBreachService(orderRepository, slaProperties, clock).findBreaches();

    assertEquals(2, report.getTotalBreaches());
    assertTrue(report.getBreaches().get(0).getAgeMinutes() >= report.getBreaches().get(1).getAgeMinutes());
  }

  @Test
  public void shouldFilterByStateAndCountOnlyThatState() {
    repositoryContains(makeOrderInState(1L, OrderState.APPROVED), makeOrderInState(2L, OrderState.PREPARING));

    SlaBreachReport report = serviceAt(45).findBreaches(OrderState.PREPARING);

    assertEquals(1, report.getTotalBreaches());
    assertEquals(Collections.singleton(OrderState.PREPARING.name()), report.getBreachCountsByState().keySet());
    assertEquals(Long.valueOf(2L), report.getBreaches().get(0).getOrderId());
  }

  @Test
  public void shouldRejectTerminalStateFilter() {
    repositoryContains();
    try {
      serviceAt(45).findBreaches(OrderState.DELIVERED);
      fail("expected TerminalOrderStateException");
    } catch (TerminalOrderStateException expected) {
    }
  }
}
