library model;

import 'dart:html';
import 'dart:convert';

class Authority {
  static const Authority NORMAL = const Authority._('NORMAL');
  static const Authority ZONE_ADMIN = const Authority._('ZONE_ADMIN');
  static const Authority ROOT = const Authority._('ROOT');
  static const List<Authority> ALL = const [NORMAL, ZONE_ADMIN, ROOT];
  final name;

  const Authority._(this.name);

  static Set<Authority> decodeSet(Set<String> raws) {
    return raws.map((s) => valueOf(s)).toSet();
  }

  static Authority valueOf(String name) {
    return ALL.firstWhere((auth) => auth.name == name);
  }

  toJson() => name;
}

class AccountAuth {
  final String accountId;
  final String name;
  final String accessToken;
  final Set<Authority> authorities;
  final DateTime expireTime;

  AccountAuth(this.accountId, this.name, this.accessToken, this.authorities, this.expireTime);

  AccountAuth.decode(Map raw) : this(//
      raw['accountId'], raw['name'], raw['accessToken'], //
      Authority.decodeSet(raw['authorities']), //
      new DateTime.fromMillisecondsSinceEpoch(raw['expireTime']));
}

class Account {
  final String accountId;
  final String name;
  final String accessToken;
  final Set<Authority> authorities;
  final DateTime expireTime;
  final DateTime lastExtend;

  Account(this.accountId, this.name, this.accessToken, this.authorities, this.expireTime,
          this.lastExtend);

  Account.decode(Map raw) : this(//
      raw['accountId'], raw['name'], raw['accessToken'], //
      Authority.decodeSet(raw['authorities']), //
      new DateTime.fromMillisecondsSinceEpoch(raw['expireTime']),
      new DateTime.fromMillisecondsSinceEpoch(raw['lastExtend']));

  toJson() => {
      'accountId':accountId, 'name':name, 'accessToken':accessToken, //
      'authorities':authorities.map((auth) => auth.toJson()).toList(), //
      'expireTime':expireTime.millisecondsSinceEpoch, //
      'lastExtend':lastExtend.millisecondsSinceEpoch
  };
}

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