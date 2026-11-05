import org.flywaydb.core.Flyway;

public class LegacyMigration {
    public static void main(String[] args) {
        Flyway flyway = Flyway.configure()
                .dataSource(args[0], args[2], args[3])
                .locations("filesystem:" + args[1])
                .schemas("ftgo")
                .load();
        flyway.migrate();
        flyway.validate();
    }
}
