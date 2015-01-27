library model_dao;

import 'account.dart';
import 'dart:html';
import 'dart:convert';

class AccountDao {
  static const String KEY = 'ACCOUNT_AUTH';
  bool _useLocalStorage = false;

  void save(AccountAuth auth, {bool permanent}) {

    if (permanent != null) {
      _useLocalStorage = permanent ;
    }

    var storage = _useLocalStorage ? window.localStorage : window.sessionStorage;
    storage[KEY] = JSON.encode(auth);
  }

  void remove() {
    window.localStorage.remove(KEY);
    window.sessionStorage.remove(KEY);
  }

  //nullable
  AccountAuth load() {
    AccountAuth loadFromStorage(Storage storage) {
      if (!storage.containsKey(KEY)) {
        return null;
      }
      try {
        return new AccountAuth.decode(JSON.decode(storage[KEY]));
      } catch (error) {
        // possible cause of decode error:
        //
        // case 1: user manually update localStorage, and it cause data corrupted
        // case 2: we update protocol (different serialization format or different json fields...etc)
        remove();
        return null;
      }
    }
    var auth = loadFromStorage(window.localStorage);
    if (auth == null) {
      _useLocalStorage = false;
      auth = loadFromStorage(window.sessionStorage);
      if (auth == null) {
        return null;
      }
    } else {
      _useLocalStorage = true;
    }

    if (auth.isExpired()) {
      remove();
      return null;
    }

    return auth;
  }
}