package net.chrisrichardson.ftgo.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.PackageMatcher;
import net.chrisrichardson.ftgo.domain.DomainConfiguration;

/**
 * Parts of the codebase that are not bounded contexts: the shared kernel every context may use,
 * and the composition root that wires the contexts into one Spring Boot application.
 */
final class ContextMap {

  static final String PROJECT_PACKAGES = "net.chrisrichardson..";

  private static final PackageMatcher PROJECT = PackageMatcher.of(PROJECT_PACKAGES);
  private static final PackageMatcher SHARED_KERNEL_PACKAGES = PackageMatcher.of("net.chrisrichardson.ftgo.common..");
  private static final PackageMatcher COMMON_SWAGGER = PackageMatcher.of("net.chrisrichardson.eventstore.examples.customersandorders.commonswagger..");
  private static final String COMPOSITION_ROOT_PACKAGE = "net.chrisrichardson.ftgo";

  static final DescribedPredicate<JavaClass> SHARED_KERNEL =
          DescribedPredicate.describe("belong to the shared kernel (ftgo-common, common-swagger, DomainConfiguration)", ContextMap::isSharedKernel);

  static final DescribedPredicate<JavaClass> COMPOSITION_ROOT =
          DescribedPredicate.describe("belong to the composition root (ftgo-application)", ContextMap::isCompositionRoot);

  static final DescribedPredicate<JavaClass> IN_A_BOUNDED_CONTEXT =
          DescribedPredicate.describe("belong to a bounded context", c -> BoundedContext.of(c).isPresent());

  static final DescribedPredicate<JavaClass> PUBLISHED_API =
          DescribedPredicate.describe("belong to a published *-service-api package",
                  c -> BoundedContext.of(c).map(context -> context.publishes(c)).orElse(false));

  private ContextMap() {
  }

  static boolean isProjectClass(JavaClass javaClass) {
    JavaClass c = normalize(javaClass);
    return !c.isPrimitive() && PROJECT.matches(c.getPackageName());
  }

  static boolean isSharedKernel(JavaClass javaClass) {
    JavaClass c = normalize(javaClass);
    return SHARED_KERNEL_PACKAGES.matches(c.getPackageName())
            || COMMON_SWAGGER.matches(c.getPackageName())
            || c.getName().equals(DomainConfiguration.class.getName());
  }

  static boolean isCompositionRoot(JavaClass javaClass) {
    return normalize(javaClass).getPackageName().equals(COMPOSITION_ROOT_PACKAGE);
  }

  /** Maps arrays to their element type and nested/anonymous classes to their top-level class. */
  static JavaClass normalize(JavaClass javaClass) {
    JavaClass c = javaClass.getBaseComponentType();
    while (c.getEnclosingClass().isPresent()) {
      c = c.getEnclosingClass().get();
    }
    return c;
  }
}
