library news_feed;

import 'dart:html';
import '../server_part_loader.dart';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

class NewsFeedComp {

  final Element elem;
  final ServerPartLoader serverPartLoader;
  final NewsFeedNotification notification;

  NewsFeedComp(this.elem, this.serverPartLoader, this.notification) {
    bool isFirstPage = elem.dataset['first-page'].toLowerCase() == 'true';
    List<FeedAssetComp> assets = elem.querySelectorAll('[feed-asset]').map((el) {
      return new FeedAssetComp(el);
    }).toList();

    new PartLoaderPager(elem, serverPartLoader, assets.isEmpty ? null : assets.last.assetId);

    if (isFirstPage && assets.isNotEmpty) {
      notification.acknowledge(assets.first.assetId);
    }
  }

}

class FeedAssetComp {
  final Element elem;
  String assetId;

  FeedAssetComp(this.elem) {
    assetId = elem.dataset['asset-id'] ;
  }
}
