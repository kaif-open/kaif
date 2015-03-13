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

  NewsFeedNotification(this.accountService, this.accountSession) {
  }

  Future<int> getNewsFeedUnread() {
    if (!accountSession.isSignIn) {
      return new Future.value(0);
    }
    return accountService.newsFeedUnread();
  }

  Future acknowledge(String assetId) {
    return accountService.newsFeedAcknowledge(assetId);
  }
}