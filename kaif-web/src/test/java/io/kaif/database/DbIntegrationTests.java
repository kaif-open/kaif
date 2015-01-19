package io.kaif.database;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import io.kaif.config.SpringProfile;
import io.kaif.config.UtilConfiguration;

@ActiveProfiles(SpringProfile.TEST)
@SpringApplicationConfiguration(classes = DbIntegrationTests.JdbcTestApplication.class)
public abstract class DbIntegrationTests extends AbstractTransactionalJUnit4SpringContextTests {

  @ComponentScan(basePackages = "io.kaif.model")
  @Import(value = { DataSourceAutoConfiguration.class,
      DataSourceTransactionManagerAutoConfiguration.class, UtilConfiguration.class })
  public static class JdbcTestApplication {

  }
}
