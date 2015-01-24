library model_account;

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
  final String name;
  final String accessToken;
  final DateTime expireTime;

  AccountAuth(this.name, this.accessToken, this.expireTime);

  AccountAuth.decode(Map raw) : this(
      raw['name'],
      raw['accessToken'],
      new DateTime.fromMillisecondsSinceEpoch(raw['expireTime']));
}

class Account {

  final String name;
  final String accessToken;
  final DateTime expireTime;
  final DateTime lastExtend;

  Account(this.name, this.accessToken, this.expireTime,
          this.lastExtend);

  Account.decode(Map raw) : this(
      raw['name'],
      raw['accessToken'],
      new DateTime.fromMillisecondsSinceEpoch(raw['expireTime']),
      new DateTime.fromMillisecondsSinceEpoch(raw['lastExtend']));

  toJson() => {
      'name':name,
      'accessToken':accessToken,
      'expireTime':expireTime.millisecondsSinceEpoch,
      'lastExtend':lastExtend.millisecondsSinceEpoch
  };

  bool isRequireExtends() {
    var now = new DateTime.now();
    return now.isAfter(lastExtend.add(const Duration(days:1)));
  }

  bool isExpired() {
    var now = new DateTime.now();
    return now.isAfter(expireTime);
  }
}