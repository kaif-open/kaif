package io.kaif.service.impl;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.Authorization;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.clientapp.ClientAppDao;
import io.kaif.service.ClientAppService;
import io.kaif.web.support.AccessDeniedException;

@Service
@Transactional
public class ClientAppServiceImpl implements ClientAppService {

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private ClientAppDao clientAppDao;

  @Override
  public ClientApp create(Authorization creator,
      String name,
      String description,
      String callbackUri) {
    //TODO check account is citezen
    Account account = accountDao.strongVerifyAccount(creator)
        .orElseThrow(() -> new AccessDeniedException("not allow create app."));
    return clientAppDao.createClientApp(account, name, description, callbackUri, Instant.now());
  }

  @Override
  public ClientApp loadClientAppWithoutCache(String clientId) {
    return clientAppDao.loadClientAppWithoutCache(clientId);
  }
}
