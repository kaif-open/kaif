library model_account;

class PermissionError extends Error {}

class AccountAuth {
  late String username;
  late String accessToken;
  late DateTime expireTime;
  late DateTime generateTime;

  AccountAuth(String? username, String? accessToken, int? expireTime,
      int? generateTime) {
    if (username == null ||
        accessToken == null ||
        expireTime == null ||
        generateTime == null) {
      //someone corrupt data, force abort
      throw new PermissionError();
    }
    this.username = username;
    this.accessToken = accessToken;
    this.expireTime = new DateTime.fromMillisecondsSinceEpoch(expireTime);
    this.generateTime = new DateTime.fromMillisecondsSinceEpoch(generateTime);
  }

  AccountAuth.decode(Map raw)
      : this(raw['username'], raw['accessToken'], raw['expireTime'],
            raw['generateTime']);

  toJson() => {
        'username': username,
        'accessToken': accessToken,
        'expireTime': expireTime.millisecondsSinceEpoch,
        'generateTime': generateTime.millisecondsSinceEpoch
      };

  bool isRequireExtends() {
    var now = new DateTime.now();
    return now.isAfter(generateTime.add(const Duration(days: 1)));
  }

  bool isExpired() {
    var now = new DateTime.now();
    return now.isAfter(expireTime);
  }
}
