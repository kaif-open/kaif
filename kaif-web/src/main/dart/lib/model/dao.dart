library model_dao;

import 'account.dart';
import 'dart:html';
import 'dart:convert';

class AccountDao {
  static const String KEY = 'ACCOUNT';
  bool _useLocalStorage = false;

  void saveAccount(AccountAuth auth, {bool rememberMe}) {

    if (rememberMe != null) {
      _useLocalStorage = rememberMe ;
    }

    var account = new Account(auth.accountId, auth.name, auth.accessToken, auth.authorities,
    auth.expireTime, new DateTime.now());

    var storage = _useLocalStorage ? window.localStorage : window.sessionStorage;
    storage[KEY] = JSON.encode(account);
  }

  void removeAccount() {
    window.localStorage.remove(KEY);
    window.sessionStorage.remove(KEY);
  }

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
      _useLocalStorage = false;
      account = load(window.sessionStorage);
      if (account == null) {
        return null;
      }
    } else {
      _useLocalStorage = true;
    }

    if (account.isExpired()) {
      removeAccount();
      return null;
    }

    return account;
  }
}