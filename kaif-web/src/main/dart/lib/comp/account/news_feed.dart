library news_feed;

import 'dart:html';
import '../server_part_loader.dart';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

class NewsFeed {

  final Element elem;
  final ServerPartLoader serverPartLoader;
  final NewsFeedNotification notification;

  NewsFeed(this.elem, this.serverPartLoader, this.notification) {
    bool isFirstPage = elem.dataset['first-page'].toLowerCase() == 'true';
    List<FeedAssetComp> assets = elem.querySelectorAll('[feed-asset]').map((el) {
      return new FeedAssetComp(el);
    }).toList();

    _initPager(assets.isEmpty ? null : assets.last.assetId);

    if (isFirstPage && assets.isNotEmpty) {
      notification.acknowledge(assets.first.assetId).then((_) {
        print('>>> acked');
      });
    }
  }

  void _initPager(String startAssetId) {
    if (startAssetId == null) {
      return;
    }
    Element pagerAnchor = elem.querySelector('[news-feed-pager]');
    if (pagerAnchor == null) {
      return;
    }

    pagerAnchor.onClick.first.then((e) {
      e
        ..preventDefault()
        ..stopPropagation();
      pagerAnchor.remove();
      Element nextWrapper = elem.querySelector('[next-news-feed]');
      //move next list to outside of current news-feed
      elementInsertAfter(elem, nextWrapper);

      //load next page, this will create another NewsFeed component
      serverPartLoader.loadInto(nextWrapper,
      route.currentPartTemplatePath() + "?startAssetId=${startAssetId}",
      loading:new Loading.largeCenter());
    });
  }
}

class FeedAssetComp {
  final Element elem;
  String assetId;

  FeedAssetComp(this.elem) {
    assetId = elem.dataset['asset-id'] ;
  }


}
