library model_account;

class PermissionError extends Error {
}

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
  final String username;
  final String accessToken;
  final DateTime expireTime;
  final DateTime generateTime;
  AccountAuth(this.username, this.accessToken, this.expireTime, this.generateTime) {
    if (username == null || accessToken == null || expireTime == null || generateTime == null) {
      //someone corrupt data, force abort
      throw new PermissionError();
    }
  }

  AccountAuth.decode(Map raw) : this(
      raw['username'],
      raw['accessToken'],
      new DateTime.fromMillisecondsSinceEpoch(raw['expireTime']),
      new DateTime.fromMillisecondsSinceEpoch(raw['generateTime']));

  toJson() => {
      'username':username,
      'accessToken':accessToken,
      'expireTime':expireTime.millisecondsSinceEpoch,
      'generateTime':generateTime.millisecondsSinceEpoch
  };

  bool isRequireExtends() {
    var now = new DateTime.now();
    return now.isAfter(generateTime.add(const Duration(days:1)));
  }

  bool isExpired() {
    var now = new DateTime.now();
    return now.isAfter(expireTime);
  }
}

