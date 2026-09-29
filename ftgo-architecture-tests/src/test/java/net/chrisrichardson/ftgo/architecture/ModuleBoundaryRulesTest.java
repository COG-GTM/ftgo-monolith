package net.chrisrichardson.ftgo.architecture;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import net.chrisrichardson.ftgo.consumerservice.domain.ConsumerService;
import net.chrisrichardson.ftgo.domain.Action;
import net.chrisrichardson.ftgo.domain.ActionType;
import net.chrisrichardson.ftgo.domain.Courier;
import net.chrisrichardson.ftgo.domain.CourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.CourierRepository;
import net.chrisrichardson.ftgo.domain.DistanceOptimizedCourierAssignmentStrategy;
import net.chrisrichardson.ftgo.domain.MenuItem;
import net.chrisrichardson.ftgo.domain.Order;
import net.chrisrichardson.ftgo.domain.Plan;
import net.chrisrichardson.ftgo.domain.Restaurant;
import net.chrisrichardson.ftgo.domain.RestaurantRepository;
import net.chrisrichardson.ftgo.orderservice.domain.OrderConfiguration;
import net.chrisrichardson.ftgo.orderservice.domain.OrderService;
import net.chrisrichardson.ftgo.orderservice.web.GetOrderResponse;
import net.chrisrichardson.ftgo.orderservice.web.MenuItemIdAndQuantity;
import net.chrisrichardson.ftgo.orderservice.web.OrderController;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static net.chrisrichardson.ftgo.architecture.ContextMap.COMPOSITION_ROOT;
import static net.chrisrichardson.ftgo.architecture.ContextMap.IN_A_BOUNDED_CONTEXT;
import static net.chrisrichardson.ftgo.architecture.ContextMap.PUBLISHED_API;
import static net.chrisrichardson.ftgo.architecture.ContextMap.SHARED_KERNEL;
import static org.junit.Assert.assertTrue;

@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = "net.chrisrichardson", importOptions = ImportOption.DoNotIncludeTests.class)
public class ModuleBoundaryRulesTest {

  @ArchTest
  static final ArchRule every_class_is_placed_on_the_context_map =
          classes().should(new ArchCondition<JavaClass>("belong to exactly one bounded context, the shared kernel or the composition root") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
              long homes = BoundedContext.of(javaClass).map(c -> 1).orElse(0)
                      + (SHARED_KERNEL.test(javaClass) ? 1 : 0)
                      + (COMPOSITION_ROOT.test(javaClass) ? 1 : 0);
              if (homes != 1) {
                events.add(SimpleConditionEvent.violated(javaClass,
                        javaClass.getName() + " is not placed on the context map (update BoundedContext/ContextMap)"));
              }
            }
          });

  @ArchTest
  static final ArchRule shared_kernel_does_not_depend_on_contexts_or_the_composition_root =
          noClasses().that(SHARED_KERNEL)
                  .should().dependOnClassesThat(IN_A_BOUNDED_CONTEXT.or(COMPOSITION_ROOT));

  @ArchTest
  static final ArchRule nothing_depends_on_the_composition_root =
          noClasses().that(not(COMPOSITION_ROOT))
                  .should().dependOnClassesThat(COMPOSITION_ROOT);

  @ArchTest
  static final ArchRule published_apis_only_depend_on_the_shared_kernel =
          classes().that(PUBLISHED_API)
                  .should(onlyHaveDependencies("only depend on their own published API and the shared kernel",
                          new KnownViolations(),
                          (origin, target) -> {
                            BoundedContext own = BoundedContext.of(origin).get();
                            return SHARED_KERNEL.test(target) || (own.contains(target) && own.publishes(target));
                          }));

  static final KnownViolations CROSS_CONTEXT_EXCLUSIONS = new KnownViolations()
          // Order -> Consumer: calls the consumer service implementation instead of an API/port
          .exclude(OrderConfiguration.class, ConsumerService.class, "order validates consumer in-process")
          .exclude(OrderService.class, ConsumerService.class, "order validates consumer in-process")
          // Order -> Restaurant: reads restaurant entities and repository directly
          .exclude(OrderConfiguration.class, RestaurantRepository.class, "order reads restaurant table")
          .exclude(OrderService.class, RestaurantRepository.class, "order reads restaurant table")
          .exclude(OrderService.class, Restaurant.class, "order prices line items from restaurant entity")
          .exclude(OrderService.class, MenuItem.class, "order prices line items from restaurant entity")
          .exclude(OrderController.class, Restaurant.class, "order response reads restaurant name")
          .exclude(Order.class, Restaurant.class, "Order entity holds @ManyToOne Restaurant")
          // Order -> Courier: schedules deliveries by mutating courier aggregates
          .exclude(OrderConfiguration.class, CourierRepository.class, "order assigns couriers")
          .exclude(OrderConfiguration.class, CourierAssignmentStrategy.class, "order assigns couriers")
          .exclude(OrderConfiguration.class, DistanceOptimizedCourierAssignmentStrategy.class, "order assigns couriers")
          .exclude(OrderService.class, CourierRepository.class, "order assigns couriers")
          .exclude(OrderService.class, CourierAssignmentStrategy.class, "order assigns couriers")
          .exclude(OrderService.class, DistanceOptimizedCourierAssignmentStrategy.class, "order computes ETA with courier helpers")
          .exclude(OrderService.class, Courier.class, "order writes courier plan")
          .exclude(OrderService.class, Action.class, "order writes courier plan")
          .exclude(OrderController.class, Courier.class, "order response exposes courier")
          .exclude(OrderController.class, Action.class, "order response exposes courier actions")
          .exclude(OrderController.class, ActionType.class, "order response exposes courier actions")
          .exclude(GetOrderResponse.class, Action.class, "order response exposes courier actions")
          .exclude(Order.class, Courier.class, "Order entity holds @ManyToOne assignedCourier")
          // Courier -> Order: courier plan/strategy entities reference the Order entity
          .exclude(Action.class, Order.class, "Action entity holds @ManyToOne Order")
          .exclude(Plan.class, Order.class, "plan looks up actions by Order")
          .exclude(Courier.class, Order.class, "courier cancels/looks up deliveries by Order")
          .exclude(CourierAssignmentStrategy.class, Order.class, "strategy signature takes Order")
          .exclude(DistanceOptimizedCourierAssignmentStrategy.class, Order.class, "strategy reads order delivery address")
          // Courier -> Restaurant
          .exclude(DistanceOptimizedCourierAssignmentStrategy.class, Restaurant.class, "strategy reads restaurant address");

  @ArchTest
  static final ArchRule contexts_only_reach_other_contexts_through_published_apis =
          classes().that(IN_A_BOUNDED_CONTEXT)
                  .should(onlyHaveDependencies("only depend on other bounded contexts through their published *-service-api",
                          CROSS_CONTEXT_EXCLUSIONS,
                          (origin, target) -> {
                            Optional<BoundedContext> targetContext = BoundedContext.of(target);
                            return !targetContext.isPresent()
                                    || targetContext.equals(BoundedContext.of(origin))
                                    || targetContext.get().publishes(target);
                          }));

  @ArchTest
  static final ArchRule service_modules_are_free_of_cycles =
          slices().matching("net.chrisrichardson.ftgo.(*service)..").should().beFreeOfCycles();

  static final KnownViolations DOMAIN_TO_WEB_EXCLUSIONS = new KnownViolations()
          .exclude(OrderService.class, MenuItemIdAndQuantity.class, "createOrder takes the web request DTO");

  @ArchTest
  static final ArchRule domain_layer_does_not_depend_on_web_layer =
          classes().that().resideInAPackage("net.chrisrichardson.ftgo.*service.domain..")
                  .should(onlyHaveDependencies("not depend on the web layer",
                          DOMAIN_TO_WEB_EXCLUSIONS,
                          (origin, target) -> !target.getPackageName().matches("net\\.chrisrichardson\\.ftgo\\.\\w+service\\.web(\\..*)?")));

  @ArchTest
  static void known_violations_are_not_stale(JavaClasses classes) {
    for (KnownViolations violations : new KnownViolations[]{CROSS_CONTEXT_EXCLUSIONS, DOMAIN_TO_WEB_EXCLUSIONS}) {
      List<String> stale = violations.all().stream()
              .filter(e -> !classes.contain(e.origin) || classes.get(e.origin).getDirectDependenciesFromSelf().stream()
                      .noneMatch(d -> ContextMap.normalize(d.getTargetClass()).getName().equals(e.target)))
              .map(Object::toString)
              .collect(Collectors.toList());
      assertTrue("Remove fixed entries from KnownViolations: " + stale, stale.isEmpty());
    }
  }

  private static ArchCondition<JavaClass> onlyHaveDependencies(String description,
                                                               KnownViolations exclusions,
                                                               BiFunction<JavaClass, JavaClass, Boolean> allowed) {
    return new ArchCondition<JavaClass>(description) {
      @Override
      public void check(JavaClass origin, ConditionEvents events) {
        for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
          JavaClass target = dependency.getTargetClass();
          if (!ContextMap.isProjectClass(target)
                  || allowed.apply(origin, target)
                  || exclusions.isExcluded(origin, target)) {
            continue;
          }
          events.add(SimpleConditionEvent.violated(dependency, dependency.getDescription()));
        }
      }
    };
  }
}
