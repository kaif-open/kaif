library service;

import 'package:kaif_web/model.dart';
import 'dart:html';
import 'dart:convert';
import 'dart:async';

class ServerType {
  String getAccountUrl(String path) => '/api/account$path';
}

abstract class _AbstractService {
  ServerType _serverType;

  _AbstractService(this._serverType);

  Future<HttpRequest> _postJson(String url, dynamic json, {Map<String, String> header}) {
    return _requestJson('POST', url, json, header:header);
  }

  Future<HttpRequest> _putJson(String url, dynamic json, {Map<String, String> header}) {
    return _requestJson('PUT', url, json, header:header);
  }

  Future<HttpRequest> _requestJson(String method, String url, dynamic json,
                                   {Map<String, String> header}) {
    if (header == null) {
      header = {
      };
    }
    header['Content-Type'] = 'application/json';
    return HttpRequest.request(url, method:method, sendData:JSON.encode(json),
    requestHeaders:header);
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