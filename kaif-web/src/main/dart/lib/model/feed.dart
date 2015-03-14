library model_feed;

import 'service.dart';
import 'session.dart';
import 'dao.dart';
import 'dart:async';

class NewsFeedNotification {

  final AccountService accountService;
  final AccountSession accountSession;
  final StreamController<int> _onUnreadChanged = new StreamController.broadcast();
  final NewsFeedDao newsFeedDao;
  static const Duration _POLL_INTERVAL = const Duration(minutes:10);

  NewsFeedNotification(this.accountService, this.accountSession, this.newsFeedDao) {
    new Timer.periodic(_POLL_INTERVAL, (timer) {
      _reloadUnread().then((value) => _onUnreadChanged.add(value));
    });
  }

  Future<int> getUnread() {
    if (!accountSession.isSignIn) {
      return new Future.value(0);
    }
    var cached = newsFeedDao.findCounter();
    if (cached != null) {
      return new Future.value(cached);
    }
    return _reloadUnread();
  }

  Future<int> _reloadUnread() {
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