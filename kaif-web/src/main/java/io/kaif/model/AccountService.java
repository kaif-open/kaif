package io.kaif.model;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;

@Service
@Transactional
public class AccountService {

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public Account createViaEmail(String name, String email, String password) {
    return accountDao.create(name, email, passwordEncoder.encode(password));
  }

  public Account findById(String accountId) {
    return accountDao.findById(UUID.fromString(accountId));
  }

  //TODO activate via email
}
