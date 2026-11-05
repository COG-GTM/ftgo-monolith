package net.chrisrichardson.ftgo;

import net.chrisrichardson.ftgo.common.PersonName;
import net.chrisrichardson.ftgo.common.tracking.ApiRequestLog;
import net.chrisrichardson.ftgo.common.tracking.ApiRequestLogRepository;
import net.chrisrichardson.ftgo.domain.Consumer;
import net.chrisrichardson.ftgo.domain.ConsumerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import jakarta.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FtgoApplicationTest.Config.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy"
    })
public class JpaSchemaValidationTest {

  @Autowired
  private JdbcTemplate jdbc;
  @Autowired
  private ConsumerRepository consumers;
  @Autowired
  private ApiRequestLogRepository logs;
  @Autowired
  private PlatformTransactionManager transactionManager;
  @Autowired
  private EntityManager entityManager;

  @Test
  public void shouldUseAndAdvanceTheExistingSequenceValueByOne() {
    long original = jdbc.queryForObject("SELECT next_val FROM hibernate_sequence", Long.class);
    long maxId = jdbc.queryForObject("SELECT COALESCE(MAX(id), 0) FROM consumers", Long.class);
    long seeded = Math.max(original, maxId) + 1000;
    jdbc.update("UPDATE hibernate_sequence SET next_val = ?", seeded);
    try {
      new TransactionTemplate(transactionManager).execute(status -> {
        status.setRollbackOnly();
        Consumer consumer = consumers.save(new Consumer(new PersonName("Schema", "Validation")));
        entityManager.flush();
        assertEquals(Long.valueOf(seeded), consumer.getId());
        assertEquals(Long.valueOf(seeded + 1),
            jdbc.queryForObject("SELECT next_val FROM hibernate_sequence", Long.class));
        return null;
      });
      assertEquals(Long.valueOf(0), jdbc.queryForObject(
          "SELECT COUNT(*) FROM consumers WHERE id = ?", Long.class, seeded));
    } finally {
      jdbc.update("UPDATE hibernate_sequence SET next_val = ?", original);
    }
  }

  @Test
  public void shouldSearchRequestUrisWithPortableJpql() {
    new TransactionTemplate(transactionManager).execute(status -> {
      status.setRollbackOnly();
      ApiRequestLog log = logs.save(new ApiRequestLog("schema-validation", "GET",
          "/schema-validation/orders", null, "localhost", "test"));
      log.complete(500, 1);
      entityManager.flush();
      List<ApiRequestLog> matches = logs.findByRequestUri("schema-validation");
      assertTrue(matches.stream().anyMatch(match -> match.getId().equals(log.getId())));
      assertTrue(logs.findByRequestUri("schema-validation-missing").isEmpty());
      assertTrue(logs.findByRequestUri("").stream().anyMatch(match -> match.getId().equals(log.getId())));
      assertTrue(logs.findByRequestUri(null).isEmpty());
      assertTrue(logs.findErrorsSince(log.getRequestTimestamp().minusSeconds(1)).stream()
          .anyMatch(match -> match.getId().equals(log.getId())));
      return null;
    });
  }
}
