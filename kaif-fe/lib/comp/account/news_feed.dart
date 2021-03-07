library news_feed;

import 'dart:html';

import 'package:kaif_web/model.dart';

import '../server_part_loader.dart';

class NewsFeedComp {
  final Element elem;
  final ServerPartLoader serverPartLoader;
  final NewsFeedNotification notification;

  NewsFeedComp(this.elem, this.serverPartLoader, this.notification) {
    bool isFirstPage = elem.dataset['first-page']?.toLowerCase() == 'true';
    List<FeedAssetComp> assets =
        elem.querySelectorAll('[feed-asset]').map((el) {
      return new FeedAssetComp(el);
    }).toList();

    if (isFirstPage && assets.isNotEmpty) {
      //mark un-read for first page only
      _markUnread(assets);

      //acknowledge only meaningful for first page
      notification.acknowledge(assets.first.assetId);
    }

    new PartLoaderPager(
        elem, serverPartLoader, assets.isEmpty ? null : assets.last.assetId);
  }

  void _markUnread(List<FeedAssetComp> assets) {
    assets
        .takeWhile((asset) => !asset.acked)
        .forEach((asset) => asset.markAsUnread());
  }
}

class FeedAssetComp {
  final Element elem;
  late String assetId;
  late bool acked;

  FeedAssetComp(this.elem) {
    assetId = elem.dataset['asset-id']!;
    acked = elem.dataset['asset-acked'] == 'true';
  }

  void markAsUnread() {
    elem.classes.add('news-feed-unread');
  }
}
