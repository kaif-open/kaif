library model_service;

import 'dart:async';
import 'dart:convert';
import 'dart:html';

import 'package:kaif_web/util.dart';

import 'account.dart';
import 'vote.dart';

class RestErrorResponse extends Error {
  final int code;
  final String reason;
  final bool translated;

  static RestErrorResponse tryDecode(String text) {
    try {
      var json = jsonDecode(text);
      if (json is Map) {
        Map raw = json;
        if (raw.containsKey('code') && raw.containsKey('reason')) {
          var tx = raw['translated'] == null ? false : raw['translated'];
          return new RestErrorResponse(raw['code'], raw['reason'], tx);
        }
      }
    } catch (e) {}
    return null;
  }

  RestErrorResponse(this.code, this.reason, this.translated);

  String toString() {
    if (translated) {
      //translated response is domain exception in server side.
      //we can display `reason` to user because it is translated.
      return reason;
    }
    return "$reason (code:$code)";
  }
}

abstract class _ModelMapper {
  T _mapToSingleWrapper<T>(HttpRequest request) {
    return jsonDecode(request.responseText)['data'] as T;
  }
}

typedef String accessTokenProvider();

abstract class _AbstractService extends Object with _ModelMapper {
  _populateAccessToken(Map<String, String> headers) {
    String token = _accessTokenProvider();
    if (token != null) {
      headers['X-KAIF-ACCESS-TOKEN'] = token;
    }
  }

  ServerType _serverType;
  accessTokenProvider _accessTokenProvider;

  _AbstractService(this._serverType, this._accessTokenProvider);

  Future<HttpRequest> _postJson(String url, dynamic json,
      {Map<String, String> header}) {
    return _requestJson('POST', url, json, header: header);
  }

  Future<HttpRequest> _deleteJson(String url, dynamic json,
      {Map<String, String> header}) {
    return _requestJson('DELETE', url, json, header: header);
  }

  Future<HttpRequest> _putJson(String url, dynamic json,
      {Map<String, String> header}) {
    return _requestJson('PUT', url, json, header: header);
  }

  Future<HttpRequest> _get(String url,
      {Map<String, Object> params: const {}, Map<String, String> header}) {
    var parts = [];
    params.forEach((key, value) {
      if (value != null) {
        String strValue = (value is Iterable)
            ? value.join(',')
            : value.toString();
        parts.add('${Uri.encodeQueryComponent(key)}='
            '${Uri.encodeQueryComponent(strValue)}');
      }
    });
    var query = parts.join('&');
    String getUrl = query.isEmpty ? url : '${url}?${query}';

    Map<String, String> requestHeaders = header == null ? {} : header;
    _populateAccessToken(requestHeaders);
    return HttpRequest.request(getUrl, requestHeaders: requestHeaders)
        .catchError(_onHandleRequestError);
  }

  Future<HttpRequest> _getPlanTextWithoutHandleError(String url) {
    Map<String, String> requestHeaders = {};
    _populateAccessToken(requestHeaders);
    return HttpRequest.request(url, requestHeaders: requestHeaders);
  }

  void _onHandleRequestError(Object raw) {
    ProgressEvent event = raw as ProgressEvent;
    HttpRequest req = event.target;
    var restErrorResponse = RestErrorResponse.tryDecode(req.responseText);
    if (restErrorResponse == null) {
      throw new RestErrorResponse(
          500, 'Unexpected error response, status:${req.status}', false);
    }
    throw restErrorResponse;
  }

  Future<HttpRequest> _requestJson(String method, String url, dynamic json,
      {Map<String, String> header}) {
    Map<String, String> requestHeaders = header == null ? {} : header;
    requestHeaders['Content-Type'] = 'application/json';
    _populateAccessToken(requestHeaders);
    return HttpRequest.request(url,
            method: method,
            sendData: jsonEncode(json),
            requestHeaders: requestHeaders)
        .catchError(_onHandleRequestError);
  }
}

class PartService extends _AbstractService {
  PartService(ServerType serverType, accessTokenProvider _provider)
      : super(serverType, _provider);

  /**
   * throw PermissionError if auth failed
   * or StateError if any server render problem
   */
  Future<String> loadPart(String partPath) {
    return _getPlanTextWithoutHandleError(partPath)
        .then((req) => req.responseText)
        .catchError((Object raw) {
      ProgressEvent event = raw as ProgressEvent;
      HttpRequest request = event.target;
      if (request.status == 401 || request.status == 403) {
        throw new PermissionError();
      } else {
        if (_serverType.isDevMode) {
          throw new StateError(
              '[DEBUG] render $partPath error, response:\n ${request.responseText}');
        }
        throw new StateError('Unexpected error, status:${request.status}');
      }
    });
  }
}

class AccountService extends _AbstractService {
  AccountService(ServerType serverType, accessTokenProvider _provider)
      : super(serverType, _provider);

  String _getUrl(String path) => '/api/account$path';

  Future createAccount(String username, String email, String password) {
    var json = {'username': username, 'email': email, 'password': password};
    return _putJson(_getUrl('/'), json).then((res) => null);
  }

  Future<bool> isNameAvailable(String username) {
    var params = {'username': username};
    return _get(_getUrl('/name-available'), params: params)
        .then((request) => _mapToSingleWrapper<bool>(request));
  }

  Future<bool> isEmailAvailable(String email) {
    var params = {'email': email};
    return _get(_getUrl('/email-available'), params: params)
        .then((request) => _mapToSingleWrapper<bool>(request));
  }

  Future<int> newsFeedUnread() {
    return _get(_getUrl('/news-feed-unread'), params: {})
        .then((request) => _mapToSingleWrapper<int>(request));
  }

  Future newsFeedAcknowledge(String assetId) {
    var json = {'assetId': assetId};
    return _postJson(_getUrl('/news-feed-acknowledge'), json)
        .then((res) => null);
  }

  Future<AccountAuth> authenticate(String username, String password) {
    var json = {'username': username, 'password': password};
    return _postJson(_getUrl('/authenticate'), json)
        .then((req) => jsonDecode(req.responseText))
        .then((raw) => new AccountAuth.decode(raw));
  }

  Future resendActivation() {
    return _postJson(_getUrl('/resend-activation'), {}).then((req) => null);
  }

  Future updatePasswordWithToken(String token, String password) {
    var json = {'token': token, 'password': password};
    return _postJson(_getUrl('/update-password-with-token'), json)
        .then((req) => null);
  }

  Future<AccountAuth> updateNewPassword(
      String oldPassword, String newPassword) {
    var json = {'oldPassword': oldPassword, 'newPassword': newPassword};
    return _postJson(_getUrl('/update-new-password'), json)
        .then((req) => jsonDecode(req.responseText))
        .then((raw) => new AccountAuth.decode(raw));
  }

  Future sendResetPassword(String username, String email) {
    var json = {'username': username, 'email': email};
    return _postJson(_getUrl('/send-reset-password'), json).then((req) => null);
  }

  Future<String> updateDescription(String description) {
    var json = {'description': description};
    return _postJson(_getUrl('/description'), json)
        .then((req) => req.responseText);
  }

  Future<String> loadEditableDescription() {
    return _get(_getUrl('/description')).then((req) => req.responseText);
  }

  Future<String> previewDescription(String description) {
    var json = {'description': description};
    return _putJson(_getUrl('/description/preview'), json)
        .then((req) => req.responseText);
  }

  Future<String> createOauthDirectAuthorizeToken() async {
    var json = {};
    return _mapToSingleWrapper<String>(
        await _putJson(_getUrl('/oauth-direct-authorize-token'), json));
  }
}

class ArticleService extends _AbstractService {
  ArticleService(ServerType serverType, accessTokenProvider _provider)
      : super(serverType, _provider);

  String _getUrl(String path) => '/api/article$path';

  Future createExternalLink(String zone, String url, String title) {
    var json = {'zone': zone, 'url': url, 'title': title};
    return _putJson(_getUrl('/external-link'), json).then((res) => null);
  }

  Future createSpeak(String zone, String content, String title) {
    var json = {'zone': zone, 'content': content, 'title': title};
    return _putJson(_getUrl('/speak'), json).then((res) => null);
  }

  Future<String> previewDebateContent(String content) {
    var json = {'content': content};
    return _putJson(_getUrl('/debate/content/preview'), json)
        .then((req) => req.responseText);
  }

  Future<String> previewSpeakContent(String content) {
    var json = {'content': content};
    return _putJson(_getUrl('/speak/preview'), json)
        .then((req) => req.responseText);
  }

  Future<String> loadEditableDebate(String debateId) {
    var params = {'debateId': debateId};
    return _get(_getUrl('/debate/content'), params: params)
        .then((req) => req.responseText);
  }

  Future<String> updateDebateContent(String debateId, String content) {
    var json = {'debateId': debateId, 'content': content};
    return _postJson(_getUrl('/debate/content'), json)
        .then((req) => req.responseText);
  }

  /**
   * return created debateId
   */
  Future<String> debate(
      String articleId, String parentDebateId, String content) {
    String parent = isStringBlank(parentDebateId) ? null : parentDebateId;
    var json = {
      'articleId': articleId,
      'parentDebateId': parent,
      'content': content
    };
    return _putJson(_getUrl('/debate'), json)
        .then((request) => _mapToSingleWrapper<String>(request));
  }

  Future<bool> canCreateArticle(String zone) {
    var params = {'zone': zone};
    return _get(_getUrl('/can-create'), params: params)
        .then((request) => _mapToSingleWrapper<bool>(request));
  }

  Future<bool> canDeleteArticle(String username, String articleId) {
    var params = {'username': username, 'articleId': articleId};
    return _get(_getUrl('/can-delete'), params: params)
        .then((request) => _mapToSingleWrapper<bool>(request));
  }

  Future<bool> isExternalUrlExist(String zone, String url) {
    var params = {'zone': zone, 'url': url};
    return _get(_getUrl('/external-link/exist'), params: params)
        .then((request) => _mapToSingleWrapper<bool>(request));
  }

  Future<List<String>> listArticleIdsByExternalLink(String zone, String url) {
    var params = {'zone': zone, 'url': url};
    return _get(_getUrl('/external-link'), params: params).then((res) async {
      return (jsonDecode(res.responseText) as List<dynamic>)
          .map((x) => x as String)
          .toList();
    });
  }

  Future deleteArticle(String articleId) {
    var json = {'articleId': articleId};
    return _deleteJson(_getUrl('/'), json).then((res) => null);
  }
}

class VoteService extends _AbstractService {
  VoteService(ServerType serverType, accessTokenProvider _provider)
      : super(serverType, _provider);

  String _getUrl(String path) => '/api/vote$path';

  Future voteArticle(VoteState newState, String articleId,
      VoteState previousState, int previousCount) {
    var json = {
      'newState': newState,
      'articleId': articleId,
      'previousState': previousState,
      'previousCount': previousCount
    };
    return _postJson(_getUrl('/article'), json).then((res) => null);
  }

  Future<List<ArticleVoter>> listArticleVoters(List<String> articleIds) {
    var params = {'articleIds': articleIds};
    return _get(_getUrl('/article-voters'), params: params)
        .then((req) => jsonDecode(req.responseText) as List<dynamic>)
        .then((List<dynamic> list) =>
            list.map((raw) => new ArticleVoter.decode(raw)).toList());
  }

  Future<List<DebateVoter>> listDebateVotersByIds(List<String> debateIds) {
    var params = {'debateIds': debateIds};
    return _get(_getUrl('/debate-voters-by-ids'), params: params)
        .then((req) => jsonDecode(req.responseText) as List<dynamic>)
        .then((List<dynamic> list) =>
            list.map((raw) => new DebateVoter.decode(raw)).toList());
  }

  Future voteDebate(VoteState newState, String debateId,
      VoteState previousState, int previousCount) {
    var json = {
      'newState': newState,
      'debateId': debateId,
      'previousState': previousState,
      'previousCount': previousCount
    };
    return _postJson(_getUrl('/debate'), json).then((res) => null);
  }

  Future<List<DebateVoter>> listDebateVoters(String articleId) {
    var params = {'articleId': articleId};

    return _get(_getUrl('/debate-voters'), params: params)
        .then((req) => jsonDecode(req.responseText) as List<dynamic>)
        .then((List<dynamic> list) =>
            list.map((raw) => new DebateVoter.decode(raw)).toList());
  }
}

class ClientAppService extends _AbstractService {
  ClientAppService(
      ServerType serverType, accessTokenProvider accessTokenProvider)
      : super(serverType, accessTokenProvider);

  String _getUrl(String path) => '/api/client-app$path';

  Future<String> create(
      String name, String description, String callbackUri) async {
    var json = {
      'name': name,
      'description': description,
      'callbackUri': callbackUri
    };
    return _mapToSingleWrapper<String>(
        await _putJson(_getUrl('/create'), json));
  }

  Future<String> update(String clientId, String name, String description,
      String callbackUri) async {
    var json = {
      'clientId': clientId,
      'name': name,
      'description': description,
      'callbackUri': callbackUri
    };
    await _postJson(_getUrl('/update'), json);
    return null;
  }

  Future revoke(String clientId) async {
    var json = {
      'clientId': clientId,
    };
    await _postJson(_getUrl('/revoke'), json);
    return null;
  }

  Future<String> generateDebugAccessToken(String clientId) async {
    var json = {
      'clientId': clientId,
    };
    return _mapToSingleWrapper<String>(
        await _postJson(_getUrl('/generate-debug-access-token'), json));
  }
}

class ZoneService extends _AbstractService {
  ZoneService(ServerType serverType, accessTokenProvider _provider)
      : super(serverType, _provider);

  String _getUrl(String path) => '/api/zone$path';

  Future<bool> canCreateZone() {
    return _get(_getUrl('/can-create'))
        .then((request) => _mapToSingleWrapper<bool>(request));
  }

  Future<bool> isZoneAvailable(String zone) {
    var params = {'zone': zone};
    return _get(_getUrl('/zone-available'), params: params)
        .then((request) => _mapToSingleWrapper<bool>(request));
  }

  Future createZone(String zone, String aliasName) {
    var json = {'zone': zone, 'aliasName': aliasName};
    return _putJson(_getUrl('/'), json).then((res) => null);
  }
}
