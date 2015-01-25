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

  Future tryLoadInto(String selector, String partPath) {
    Element found = querySelector(selector);
    if (found == null) {
      return new Future.value(null);
    }
    //TODO handle error
    return _partService.loadPart(partPath).then((htmlText) {
      trustInnerHtml(found, htmlText);
      return found;
    }).then(_componentsInitializer);
  }
}