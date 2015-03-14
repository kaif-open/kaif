library model_feed;

import 'account.dart';
import 'service.dart';
import 'session.dart';
import 'dao.dart';
import 'dart:async';
import 'dart:convert';
import 'dart:html';

class NewsFeedNotification {

  final AccountService accountService;
  final AccountSession accountSession;
  final StreamController<int> _onUnreadChanged = new StreamController.broadcast();

  NewsFeedNotification(this.accountService, this.accountSession) {
  }

  Future<int> getUnread() {
    if (!accountSession.isSignIn) {
      return new Future.value(0);
    }
    return accountService.newsFeedUnread();
  }

  Stream get onUnreadChanged => _onUnreadChanged.stream;

  Future acknowledge(String assetId) {
    return accountService.newsFeedAcknowledge(assetId).then((v) {
      _onUnreadChanged.add(0);
      return v;
    });
  }
}