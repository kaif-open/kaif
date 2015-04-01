library developer_client_app;
import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/comp/developer/create_client_app_form.dart';
import 'package:kaif_web/model.dart';

class DeveloperClientApp {
  final Element elem;

  DeveloperClientApp(this.elem, ClientAppService clientAppService) {
    new Tabs(elem);
    new CreateClientAppForm(elem.querySelector('[create-client-app-form]'), clientAppService);
  }
}