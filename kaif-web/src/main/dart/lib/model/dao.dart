library model_dao;

import 'account.dart';
import 'dart:html';
import 'dart:convert';

class AccountDao {
  static const String KEY = 'ACCOUNT';

  void saveAccount(AccountAuth auth, {bool rememberMe}) {
    var account = new Account(auth.accountId, auth.name, auth.accessToken, auth.authorities,
    auth.expireTime, new DateTime.now());

    var storage = rememberMe ? window.localStorage : window.sessionStorage;
    storage[KEY] = JSON.encode(account);
  }

  void removeAccount() {
    window.localStorage.remove(KEY);
    window.sessionStorage.remove(KEY);
  }

  //TODO loadAccount should extends accessToken
  //nullable
  Account loadAccount() {
    Account load(Storage storage) {
      if (!storage.containsKey(KEY)) {
        return null;
      }
      return new Account.decode(JSON.decode(storage[KEY]));
    }
    var account = load(window.localStorage);
    if (account == null) {
      account = load(window.sessionStorage);
      if (account == null) {
        return null;
      }
    }

    var now = new DateTime.now();
    if (now.isAfter(account.expireTime)) {
      removeAccount();
      return null;
    }

    return account;
  }
}