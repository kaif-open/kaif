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
  final NewsFeedDao newsFeedDao;

  NewsFeedNotification(this.accountService, this.accountSession, this.newsFeedDao) {
  }

  Future<int> getUnread() {
    if (!accountSession.isSignIn) {
      return new Future.value(0);
    }
    var cached = newsFeedDao.findCounter();
    if (cached != null) {
      return new Future.value(cached);
    }
    return accountService.newsFeedUnread().then((value) {
      newsFeedDao.saveCounter(value);
      return value;
    });
  }

  Stream get onUnreadChanged => _onUnreadChanged.stream;

  Future acknowledge(String assetId) {
    return accountService.newsFeedAcknowledge(assetId).then((v) {
      newsFeedDao.saveCounter(0);
      _onUnreadChanged.add(0);
      return v;
    });
  }
}