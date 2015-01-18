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

  public Account create(String email, String password, String name) {
    return accountDao.create(email, passwordEncoder.encode(password), name);
  }

  public Account findById(String accountId) {
    return accountDao.findById(UUID.fromString(accountId));
  }
}
