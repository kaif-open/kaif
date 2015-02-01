package io.kaif.test;

import java.time.Instant;
import java.util.EnumSet;

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
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import io.kaif.config.ModelConfiguration;
import io.kaif.config.SpringProfile;
import io.kaif.config.UtilConfiguration;
import io.kaif.mail.MailAgent;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.Authority;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;

@ActiveProfiles(SpringProfile.TEST)
@SpringApplicationConfiguration(classes = DbIntegrationTests.JdbcTestApplication.class)
public abstract class DbIntegrationTests extends AbstractTransactionalJUnit4SpringContextTests
    implements ModelFixture {

  @Profile(SpringProfile.TEST)
  @ComponentScan(basePackages = { "io.kaif.model", "io.kaif.service" })
  @Import(value = { DataSourceAutoConfiguration.class,
      DataSourceTransactionManagerAutoConfiguration.class, ModelConfiguration.class,
      UtilConfiguration.class, MockTestConfig.class })
  @Configuration
  public static class JdbcTestApplication {

  }

  @Profile(SpringProfile.TEST)
  @Configuration
  static class MockTestConfig {
    @Bean
    public MailAgent mailAgent() {
      return Mockito.mock(MailAgent.class);
    }
  }

  @Autowired
  protected MailAgent mockMailAgent;

  @Autowired
  private ZoneDao zoneDao;

  @Autowired
  private AccountDao accountDao;

  @Before
  public void integrationSetUp() throws Exception {
    Mockito.reset(mockMailAgent);
  }

  protected final ZoneInfo savedZoneDefault(String zone) {
    return zoneDao.create(zoneDefault(zone));
  }

  protected final Account savedAccountCitizen(String username) {
    Account account = savedAccountTourist(username);
    accountDao.updateAuthorities(account.getAccountId(),
        EnumSet.of(Authority.CITIZEN, Authority.TOURIST));
    return accountDao.findById(account.getAccountId()).get();
  }

  protected final Account savedAccountTourist(String username) {
    return accountDao.create(username, username + "@example.com", username + "pwd", Instant.now());
  }
}
