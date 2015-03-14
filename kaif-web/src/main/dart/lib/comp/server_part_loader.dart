library server_part_loader;
import 'dart:async';
import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

typedef componentsInitializer(dynamic parentElement);

class ServerPartLoader {

  PartService _partService;
  var _componentsInitializer;

  ServerPartLoader(this._partService, this._componentsInitializer);

  Future loadInto(Element elem, String partPath, {Loading loading}) {
    Loading progress = loading == null ? new Loading.none() : loading;
    progress.renderAppend(elem, delay:const Duration(milliseconds:500));

    return _partService.loadPart(partPath).then((htmlText) {
      progress.remove();
      // server returned html soup. note all js script will be removed
      unSafeInnerHtml(elem, htmlText);
      return elem;
    }).then(_componentsInitializer)
    .catchError((permissionError) {
      new LargeErrorModal(i18n('part-loader.permission-error')).render();
      return null;
    }, test:(error) => error is PermissionError)
    .catchError((StateError stateError) {
      new Toast.error(stateError.message).render();
      return null;
    });

  }

  /**
   * load server render page into selector element.
   *
   * note that server returned html will be sanitize <script> and any
   * onFoo="" attribute
   *
   * see view/htmls.dart
   */
  Future tryLoadInto(String selector, String partPath, {Loading loading}) {
    Element found = querySelector(selector);
    if (found == null) {
      return new Future.value(null);
    }
    return loadInto(found, partPath, loading:loading);
  }
}

class PartLoaderPager {

  PartLoaderPager(Element parentElem, ServerPartLoader serverPartLoader, String nextStart) {
    if (isStringBlank(nextStart)) {
      return;
    }
    Element pagerAnchor = parentElem.querySelector('[ajax-pager]');
    if (pagerAnchor == null) {
      return;
    }
    pagerAnchor.onClick.first.then((e) {
      e
        ..preventDefault()
        ..stopPropagation();
      pagerAnchor.remove();
      Element nextWrapper = new DivElement();
      //move next list to outside of current list
      elementInsertAfter(parentElem, nextWrapper);

      //load next page, this will create another part
      serverPartLoader.loadInto(nextWrapper,
      route.currentPartTemplatePath() + "?start=${nextStart}",
      loading:new Loading.largeCenter());
    });
  }
}

