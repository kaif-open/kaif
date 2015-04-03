package io.kaif.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.Authority;
import io.kaif.model.account.Authorization;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.clientapp.ClientAppDao;
import io.kaif.model.exception.ClientAppMaxException;
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
      String callbackUri) throws ClientAppMaxException {
    Account account = verifyDeveloper(creator);
    if (listClientApps(account).size() >= ClientApp.MAX_NO_OF_APPS) {
      throw new ClientAppMaxException(ClientApp.MAX_NO_OF_APPS);
    }
    return clientAppDao.create(account, name, description, callbackUri, Instant.now());
  }

  private Account verifyDeveloper(Authorization creator) {
    return accountDao.strongVerifyAccount(creator)
        .filter(a -> a.containsAuthority(Authority.CITIZEN))
        .orElseThrow(() -> new AccessDeniedException("no authority on client app."));
  }

  @Override
  public ClientApp loadClientAppWithoutCache(String clientId) {
    return clientAppDao.loadWithoutCache(clientId);
  }

  @Override
  public List<ClientApp> listClientApps(Authorization creator) {
    return clientAppDao.listOrderByTime(creator.authenticatedId());
  }

  @Override
  public void update(Authorization creator,
      String clientId,
      String name,
      String description,
      String callbackUri) {
    Account account = verifyDeveloper(creator);
    ClientApp clientApp = clientAppDao.loadWithoutCache(clientId);
    if (!account.belongToAccount(clientApp.getOwnerAccountId())) {
      throw new AccessDeniedException("not client app owner");
    }
    clientAppDao.update(clientApp.withName(name)
        .withDescription(description)
        .withCallbackUri(callbackUri));
  }

  @Override
  public Optional<ClientApp> verifyRedirectUri(String clientId, String redirectUri) {
    return clientAppDao.find(clientId).filter(app -> app.validateRedirectUri(redirectUri));
  }
}
