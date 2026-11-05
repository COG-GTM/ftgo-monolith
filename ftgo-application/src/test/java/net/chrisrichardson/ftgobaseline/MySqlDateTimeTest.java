package net.chrisrichardson.ftgobaseline;

import net.chrisrichardson.ftgo.domain.Courier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = MySqlDateTimeTest.Config.class)
public class MySqlDateTimeTest {
    @Configuration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = Courier.class)
    static class Config {
    }

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    public void shouldPreserveLocalDateTimeInStoredDatetimeAcrossSessionTimeZones() {
        String originalTimeZone = jdbc.queryForObject("SELECT @@session.time_zone", String.class);
        DateTimeFormatter sqlFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        try {
            for (String timeZone : new String[]{"+00:00", "+05:30", "-08:00"}) {
                jdbc.execute("SET time_zone = '" + timeZone + "'");
                for (String value : new String[]{"2026-01-15T12:34:56", "2026-03-08T01:30:00",
                        "2026-03-08T03:30:00", "2026-07-15T12:34:56", "2026-11-01T01:30:00"}) {
                    LocalDateTime expected = LocalDateTime.parse(value);
                    Courier courier = new Courier();
                    ReflectionTestUtils.setField(courier, "lastLocationUpdate", expected);
                    entityManager.persist(courier);
                    entityManager.flush();
                    assertEquals(expected.format(sqlFormat), jdbc.queryForObject(
                            "SELECT DATE_FORMAT(last_location_update, '%Y-%m-%d %H:%i:%s') FROM courier WHERE id = ?",
                            String.class, courier.getId()));
                    entityManager.clear();
                    assertEquals(expected, entityManager.find(Courier.class, courier.getId()).getLastLocationUpdate());
                }
            }
        } finally {
            jdbc.update("SET time_zone = ?", originalTimeZone);
        }
    }
}
