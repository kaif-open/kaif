library model_dao;

import 'account.dart';
import 'dart:html';
import 'dart:convert';

class AccountDao {
  static const String _KEY = 'ACCOUNT_AUTH';
  bool _useLocalStorage = false;

  void save(AccountAuth auth, {bool permanent}) {

    if (permanent != null) {
      _useLocalStorage = permanent;
    }

    var storage = _useLocalStorage ? window.localStorage : window.sessionStorage;
    storage[_KEY] = JSON.encode(auth);
  }

  void remove() {
    window.localStorage.remove(_KEY);
    window.sessionStorage.remove(_KEY);
  }

  // return null if not found
  AccountAuth find() {
    AccountAuth loadFromStorage(Storage storage) {
      if (!storage.containsKey(_KEY)) {
        return null;
      }
      try {
        return new AccountAuth.decode(JSON.decode(storage[_KEY]));
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

class NewsFeedDao {
  static const String _KEY = 'NEWS_FEED';
  static const Duration _EXPIRE = const Duration(minutes:1);

  void saveCounter(int value) {
    window.localStorage[_KEY] = JSON.encode({
      'counter': value,
      'updateTime': new DateTime.now().millisecondsSinceEpoch
    });
  }

  // return null if not exist
  int findCounter() {
    if (!window.localStorage.containsKey(_KEY)) {
      return null;
    }
    try {
      var saved = JSON.decode(window.localStorage[_KEY]);
      var updateTime = new DateTime.fromMillisecondsSinceEpoch(saved['updateTime']);
      if (updateTime.add(_EXPIRE).isBefore(new DateTime.now())) {
        //expired
        return null;
      }
      //extra validate here because user may corrupt localStorage
      int counter = saved['counter'];
      if (counter > 11 || counter < 0) {
        return null;
      }
      return counter;
    } catch (e) {
      // someone corrupt localStorage, abort
      clear();
      return null;
    }
  }

  void clear() {
    window.localStorage.remove(_KEY);
  }
}