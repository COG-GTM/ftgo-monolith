package net.chrisrichardson.ftgo.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchUnitRunner;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideOutsideOfPackage;
import static com.tngtech.archunit.lang.conditions.ArchConditions.onlyHaveDependenciesWhere;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Enforces the module boundaries of the FTGO monolith. See ftgo-architecture-tests/README.md.
 */
@RunWith(ArchUnitRunner.class)
@AnalyzeClasses(packages = ModuleBoundariesTest.ROOT_PACKAGE, importOptions = ImportOption.DoNotIncludeTests.class)
public class ModuleBoundariesTest {

  static final String ROOT_PACKAGE = "net.chrisrichardson.ftgo";

  static final ServiceModule CONSUMER = new ServiceModule("ftgo-consumer-service", "consumerservice", "consumerservice.api");
  static final ServiceModule ORDER = new ServiceModule("ftgo-order-service", "orderservice", "orderservice.api");
  static final ServiceModule RESTAURANT = new ServiceModule("ftgo-restaurant-service", "restaurantservice", "restaurantservice.events");
  static final ServiceModule COURIER = new ServiceModule("ftgo-courier-service", "courierservice", "courierservice.api");

  static final String SPRING_CONFIGURATION = "org.springframework.context.annotation.Configuration";

  /**
   * Known violations that are accepted for now. Each entry is "originClass -> targetClass".
   * Remove entries as they are fixed; do not add new ones without documenting them in the README.
   */
  static final Set<String> KNOWN_VIOLATIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
          // GlobalExceptionHandler maps service-internal exceptions to HTTP responses.
          "net.chrisrichardson.ftgo.GlobalExceptionHandler -> net.chrisrichardson.ftgo.orderservice.domain.OrderNotFoundException",
          "net.chrisrichardson.ftgo.GlobalExceptionHandler -> net.chrisrichardson.ftgo.orderservice.domain.RestaurantNotFoundException",
          "net.chrisrichardson.ftgo.GlobalExceptionHandler -> net.chrisrichardson.ftgo.courierservice.domain.CourierNotFoundException"
  )));

  @ArchTest
  static final ArchRule consumer_service_internals_are_private = internalsArePrivate(CONSUMER);

  @ArchTest
  static final ArchRule order_service_internals_are_private = internalsArePrivate(ORDER);

  @ArchTest
  static final ArchRule restaurant_service_internals_are_private = internalsArePrivate(RESTAURANT);

  @ArchTest
  static final ArchRule courier_service_internals_are_private = internalsArePrivate(COURIER);

  @ArchTest
  static final ArchRule api_modules_only_depend_on_common =
          classes().that().resideInAnyPackage(apiPackages())
                  .should().onlyDependOnClassesThat(resideOutsideOfPackage(ROOT_PACKAGE + "..")
                          .or(resideInAnyPackage(apiPackagesAnd(ROOT_PACKAGE + ".common.."))))
                  .as("Service API modules should only depend on ftgo-common and other service API modules");

  @ArchTest
  static final ArchRule shared_modules_do_not_depend_on_services =
          noClasses().that().resideInAnyPackage(ROOT_PACKAGE + ".common..", ROOT_PACKAGE + ".domain..", ROOT_PACKAGE + ".testutil..")
                  .should().dependOnClassesThat().resideInAnyPackage(
                          CONSUMER.rootPattern(), ORDER.rootPattern(), RESTAURANT.rootPattern(), COURIER.rootPattern())
                  .as("Shared modules (ftgo-common, ftgo-domain, ftgo-test-util) should not depend on service modules");

  @ArchTest
  static final ArchRule service_modules_are_free_of_cycles =
          slices().matching(ROOT_PACKAGE + ".(*service)..")
                  .should().beFreeOfCycles()
                  .as("Service modules should not have cyclic dependencies");

  static ArchRule internalsArePrivate(ServiceModule module) {
    return classes().that().resideOutsideOfPackage(module.rootPattern())
            .should(onlyHaveDependenciesWhere(targetIsNotInternalTo(module)
                    .or(compositionRootWiring())
                    .or(knownViolation())))
            .as(String.format("Only %s may access its internals (%s); other modules must use %s",
                    module.gradleModule, module.rootPattern(), module.apiPattern()));
  }

  private static DescribedPredicate<Dependency> targetIsNotInternalTo(ServiceModule module) {
    return new DescribedPredicate<Dependency>("target is not internal to " + module.gradleModule) {
      @Override
      public boolean test(Dependency dependency) {
        return !module.isInternal(dependency.getTargetClass().getBaseComponentType());
      }
    };
  }

  /**
   * ftgo-application (the root package) is the composition root and may import each module's Spring configuration.
   */
  private static DescribedPredicate<Dependency> compositionRootWiring() {
    return new DescribedPredicate<Dependency>("origin is the composition root and target is a @Configuration class") {
      @Override
      public boolean test(Dependency dependency) {
        return dependency.getOriginClass().getPackageName().equals(ROOT_PACKAGE)
                && dependency.getTargetClass().getBaseComponentType().isAnnotatedWith(SPRING_CONFIGURATION);
      }
    };
  }

  private static DescribedPredicate<Dependency> knownViolation() {
    return new DescribedPredicate<Dependency>("dependency is a documented known violation") {
      @Override
      public boolean test(Dependency dependency) {
        return KNOWN_VIOLATIONS.contains(dependency.getOriginClass().getName()
                + " -> " + dependency.getTargetClass().getBaseComponentType().getName());
      }
    };
  }

  private static String[] apiPackages() {
    return apiPackagesAnd();
  }

  private static String[] apiPackagesAnd(String... others) {
    String[] apis = {CONSUMER.apiPattern(), ORDER.apiPattern(), RESTAURANT.apiPattern(), COURIER.apiPattern()};
    String[] result = Arrays.copyOf(apis, apis.length + others.length);
    System.arraycopy(others, 0, result, apis.length, others.length);
    return result;
  }

  static final class ServiceModule {
    final String gradleModule;
    final String rootPackage;
    final String apiPackage;

    ServiceModule(String gradleModule, String relativeRootPackage, String relativeApiPackage) {
      this.gradleModule = gradleModule;
      this.rootPackage = ROOT_PACKAGE + "." + relativeRootPackage;
      this.apiPackage = ROOT_PACKAGE + "." + relativeApiPackage;
    }

    String rootPattern() {
      return rootPackage + "..";
    }

    String apiPattern() {
      return apiPackage + "..";
    }

    boolean isInternal(JavaClass javaClass) {
      String pkg = javaClass.getPackageName();
      return isInPackage(pkg, rootPackage) && !isInPackage(pkg, apiPackage);
    }

    private static boolean isInPackage(String pkg, String parent) {
      return pkg.equals(parent) || pkg.startsWith(parent + ".");
    }
  }
}
