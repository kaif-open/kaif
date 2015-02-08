library model_account;

class PermissionError extends Error {
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


