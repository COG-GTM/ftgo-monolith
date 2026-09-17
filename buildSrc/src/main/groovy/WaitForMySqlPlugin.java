import org.gradle.api.Plugin;
import org.gradle.api.Project;

/** Registers the `waitForMySql` task that blocks until the MySQL database accepts connections. */
public class WaitForMySqlPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    // register() is the lazy, non-deprecated replacement for create()
    project.getTasks().register("waitForMySql", WaitForMySql.class);
  }
}
