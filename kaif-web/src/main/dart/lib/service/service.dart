library service;

import 'package:kaif_web/service/server_type.dart';
import 'package:kaif_web/model.dart';
import 'dart:html';
import 'dart:convert';
import 'dart:async';

abstract class _AbstractService {
  ServerType _serverType;

  _AbstractService(this._serverType);

  Future<HttpRequest> _postJson(String url, dynamic json, {Map<String, String> header}) {
    return HttpRequest.request(url, method:'POST', mimeType:'applicaiton/json',
    sendData:JSON.encode(json), requestHeaders:header);
  }

  Future<HttpRequest> _putJson(String url, dynamic json) {
    return HttpRequest.request(url, method:'PUT', mimeType:'applicaiton/json',
    sendData:JSON.encode(json));
  }
}

class AccountService extends _AbstractService {
  AccountService(ServerType serverType) : super(serverType);

  Future createAccount(String name, String email, String password) {
    var json = {
        'name':name, 'email':email, 'password':password
    };

    return _putJson(_serverType.getAccountUrl('/'), json).then((res) => null);
  }

  Future<AccountAuth> authenticate(String name, String password) {
    var json = {
        'name':name, 'password':password
    };
    return _postJson(_serverType.getAccountUrl('/authenticate'), json).then((
        req) => JSON.decode(req.responseText)).then((raw) => new AccountAuth.decode(raw));
  }

  Future<AccountAuth> extendsAccessToken(String accessToken) {
    var headers = {
        'X-KAIF-ACCESS-TOKEN':accessToken
    };
    return _postJson(_serverType.getAccountUrl('/extends-access-token'), {
    }, header:headers).then((req) => JSON.decode(req.responseText)).then((
        raw) => new AccountAuth.decode(raw));
  }
}