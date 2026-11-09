package net.chrisrichardson.ftgo.common;

import org.apache.catalina.util.ServerInfo;
import org.junit.Test;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;

import static org.junit.Assert.assertTrue;

/**
 * Guards against regressing to end-of-life framework versions
 * (previously Spring Boot 2.0.3 / Spring 5.0 / Tomcat 8.5 / Jackson 2.9).
 */
public class SupportedFrameworkVersionsTest {

  @Test
  public void springBootIsOnSupportedLine() {
    assertAtLeast("Spring Boot", SpringBootVersion.getVersion(), 4, 0);
  }

  @Test
  public void springFrameworkIsOnSupportedLine() {
    assertAtLeast("Spring Framework", SpringVersion.getVersion(), 7, 0);
  }

  @Test
  public void embeddedTomcatIsOnSupportedLine() {
    assertAtLeast("Tomcat", ServerInfo.getServerNumber(), 11, 0);
  }

  @Test
  public void jacksonIsOnSupportedLine() {
    assertAtLeast("Jackson", tools.jackson.databind.cfg.PackageVersion.VERSION.toString(), 3, 0);
  }

  private static void assertAtLeast(String component, String version, int major, int minor) {
    String[] parts = version.split("[.-]");
    int actualMajor = Integer.parseInt(parts[0]);
    int actualMinor = Integer.parseInt(parts[1]);
    assertTrue(component + " " + version + " is older than " + major + "." + minor,
            actualMajor > major || (actualMajor == major && actualMinor >= minor));
  }
}
