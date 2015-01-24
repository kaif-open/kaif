package io.kaif.database;

import org.junit.Before;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import io.kaif.config.SpringProfile;
import io.kaif.config.UtilConfiguration;
import io.kaif.mail.MailAgent;

@ActiveProfiles(SpringProfile.TEST)
@SpringApplicationConfiguration(classes = DbIntegrationTests.JdbcTestApplication.class)
public abstract class DbIntegrationTests extends AbstractTransactionalJUnit4SpringContextTests {

  @ComponentScan(basePackages = "io.kaif.model")
  @Import(value = { DataSourceAutoConfiguration.class,
      DataSourceTransactionManagerAutoConfiguration.class, UtilConfiguration.class,
      MockTestConfig.class })
  public static class JdbcTestApplication {

  }

  @Configuration
  public static class MockTestConfig {
    @Bean
    public MailAgent mailAgent() {
      return Mockito.mock(MailAgent.class);
    }
  }

  @Autowired
  private MailAgent mailAgent;

  @Before
  public void integrationSetUp() throws Exception {
    Mockito.reset(mailAgent);
  }
}
