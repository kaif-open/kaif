library server_part_loader;
import 'dart:async';
import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

typedef componentsInitializer(dynamic parentElement);

class ServerPartLoader {
  static const String LOADING_LARGE = 'LOADING_LARGE';
  static const String LOADING_SMALL = 'LOADING_SMALL';
  static const String LOADING_NONE = 'LOADING_NONE';
  PartService _partService;
  var _componentsInitializer;

  ServerPartLoader(this._partService, this._componentsInitializer);

  Future tryLoadInto(String selector, String partPath, {String loadingType:LOADING_NONE}) {
    Element found = querySelector(selector);
    if (found == null) {
      return new Future.value(null);
    }
    switch (loadingType) {
      case LOADING_LARGE:
        new LargeCenterLoading().renderInTo(found);
        break;
      case LOADING_SMALL:
        new SmallLoading().renderInTo(found);
        break;
      default:
        break;
    }
    return _partService.loadPart(partPath).then((htmlText) {
      trustInnerHtml(found, htmlText);
      return found;
    }).then(_componentsInitializer)
    .catchError((permissionError) {
      new LargeErrorModal(i18n('part-loader.permission_error')).render();
      return null;
    }, test:(error) => error is PermissionError)
    .catchError((StateError stateError) {
      new Toast.error(stateError.message, const Duration(seconds:10)).render();
      return null;
    });
  }
}

