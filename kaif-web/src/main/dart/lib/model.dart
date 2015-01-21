library model;

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
      Authority.decodeSet(raw['authorites']), //
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
      Authority.decodeSet(raw['authorites']), //
      new DateTime.fromMillisecondsSinceEpoch(raw['expireTime']),
      new DateTime.fromMillisecondsSinceEpoch(raw['lastExtend']));

  toJson() => {
      'accountId':accountId, 'name':name, 'accessToken':accessToken, //
      'authorites':authorities, 'expireTime':expireTime, 'lastExtend':lastExtend
  };
}