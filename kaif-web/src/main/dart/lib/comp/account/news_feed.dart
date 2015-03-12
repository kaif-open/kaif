library news_feed;

import 'dart:html';
import '../server_part_loader.dart';
import 'package:kaif_web/util.dart';

class NewsFeed {

  final Element elem;
  final ServerPartLoader serverPartLoader;

  NewsFeed(this.elem,
           this.serverPartLoader) {

    List<FeedAssetComp> assets = elem.querySelectorAll('[feed-asset]').map((el) {
      return new FeedAssetComp(el);
    }).toList();

    _initPager(assets.isEmpty ? null : assets.last.assetId);
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
      //note this searching globally because we need it to be outside of component
      Element nextWrapper = elem.querySelector('[next-news-feed]');
      //move next list to outside of current news-feed
      elementInsertAfter(elem, nextWrapper);

      //load next page, this will create another newsFeed
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
