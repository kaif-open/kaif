package io.kaif.database;

import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import io.kaif.config.SpringProfile;
import io.kaif.config.UtilConfigration;

@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles(SpringProfile.TEST)
@SpringApplicationConfiguration(classes = DbIntegrationTests.JdbcTestApplication.class)
public abstract class DbIntegrationTests {

  @ComponentScan(basePackages = "io.kaif.model")
  @Import(value = { DataSourceAutoConfiguration.class, UtilConfigration.class })
  public static class JdbcTestApplication {

  }
}
