library developer_client_app;
import 'dart:html';
import 'package:kaif_web/comp/server_part_loader.dart';
import 'package:kaif_web/util.dart';

class DeveloperClientApp {
  final Element elem;
  final ServerPartLoader serverPartLoader;

  DeveloperClientApp(this.elem, this.serverPartLoader) {
    new Tabs(elem);
  }
}