package net.chrisrichardson.ftgo.architecture;

import com.tngtech.archunit.core.domain.JavaClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class-to-class dependencies that break a module-boundary rule today and are tolerated until
 * the seam is cut. Any dependency not listed here fails the build, and an entry whose
 * dependency no longer exists also fails the build so the list can only shrink.
 */
final class KnownViolations {

  static final class Exclusion {
    final String origin;
    final String target;
    final String reason;

    Exclusion(Class<?> origin, Class<?> target, String reason) {
      this.origin = origin.getName();
      this.target = target.getName();
      this.reason = reason;
    }

    boolean matches(JavaClass origin, JavaClass target) {
      return this.origin.equals(ContextMap.normalize(origin).getName())
              && this.target.equals(ContextMap.normalize(target).getName());
    }

    @Override
    public String toString() {
      return origin + " -> " + target + " (" + reason + ")";
    }
  }

  private final List<Exclusion> exclusions = new ArrayList<>();

  KnownViolations exclude(Class<?> origin, Class<?> target, String reason) {
    exclusions.add(new Exclusion(origin, target, reason));
    return this;
  }

  boolean isExcluded(JavaClass origin, JavaClass target) {
    return exclusions.stream().anyMatch(e -> e.matches(origin, target));
  }

  List<Exclusion> all() {
    return Collections.unmodifiableList(exclusions);
  }
}
