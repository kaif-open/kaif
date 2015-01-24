library model_session;

import 'account.dart';
import 'dao.dart';
import 'dart:async';
import 'dart:convert';
import 'dart:html';

class PermissionError extends Error {
}

class AccountSession {

  final AccountDao accountDao;
  Account _current;

  AccountSession(this.accountDao) {
    _current = accountDao.loadAccount();
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
      accountDao.saveAccount(renewAuth);
      _current = accountDao.loadAccount();
      return true;
    }).catchError((error) => false, test:(error) => error is! PermissionError);
  }

  //null if not sign in
  Account get current => _current;

  String provideAccessToken() {
    return _current != null ? _current.accessToken : null;
  }

  Future<AccountAuth> _extendsAccessToken(Account exist) {
    var headers = {
        'X-KAIF-ACCESS-TOKEN':exist.accessToken,
        'Content-Type': 'application/json'
    };
    var json = {
        'accountId': exist.accountId
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
    accountDao.removeAccount();
  }
}