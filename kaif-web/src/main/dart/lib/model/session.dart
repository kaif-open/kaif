library model_session;

import 'account.dart';
import 'dao.dart';
import 'dart:async';
import 'dart:convert';
import 'dart:html';
import 'package:cookie/cookie.dart' as cookie;

class AccountSession {

  final AccountDao accountDao;
  AccountAuth _current;

  AccountSession(this.accountDao) {
    _current = accountDao.find();
    _detectForceLogout();
  }

  void _detectForceLogout() {
    // see AccountController.java #activation
    var value = cookie.get("force-logout");
    if (value != null && "true" == value.toLowerCase()) {
      cookie.remove("force-logout", path:"/", secure:true);
      signOut();
    }
  }

  void save(AccountAuth auth, {bool rememberMe}) {
    accountDao.save(auth, permanent:rememberMe);
    _current = accountDao.find();
  }

  /**
   * return true if extends, false if unchanged. caller should handle PermissionError
   */
  Future<bool> extendsTokenIfRequired() {
    if (_current == null || !_current.isRequireExtends()) {
      return new Future.value(false);
    }

    //TODO broadcast changed ?
    return _extendsAccessToken(_current).then((renewAuth) {
      save(renewAuth);
      return true;
    }).catchError((error) => false, test:(error) => error is! PermissionError);
  }

  //null if not sign in
  AccountAuth get current => _current;

  bool get isSignIn => current != null;

  bool isSelf(String username) {
    return isSignIn && username == current.username;
  }

  String provideAccessToken() {
    return _current != null ? _current.accessToken : null;
  }

  Future<AccountAuth> _extendsAccessToken(AccountAuth exist) {
    var headers = {
      'X-KAIF-ACCESS-TOKEN':exist.accessToken,
      'Content-Type': 'application/json'
    };

    //empty json body may cause problem, so fill some garbage
    var json = {
      'username': exist.username
    };

    return HttpRequest.request(
        '/api/account/extends-access-token',
        method:'POST',
        sendData:JSON.encode(json),
        requestHeaders:headers)
    .catchError((ProgressEvent event) {
      HttpRequest req = event.target;
      if (req.status == 401 || req.status == 403) {
        throw new PermissionError();
      }
      throw new StateError('abort');
    })
    .then((req) => JSON.decode(req.responseText))
    .then((raw) => new AccountAuth.decode(raw));
  }

  void signOut() {
    _current = null;
    accountDao.remove();
  }

}