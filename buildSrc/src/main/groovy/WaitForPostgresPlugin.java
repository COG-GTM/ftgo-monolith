import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class WaitForPostgresPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getTasks().create("waitForPostgres", WaitForPostgres.class);
  }
}
