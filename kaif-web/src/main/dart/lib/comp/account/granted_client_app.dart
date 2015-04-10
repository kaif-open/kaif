library granted_client_app;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';
import 'dart:async';

class GrantedClientApp {

  final Element elem;
  final ClientAppService clientAppService;

  GrantedClientApp(this.elem, this.clientAppService) {

    elem.querySelectorAll('[data-client-id]').forEach((el) {
      el.onClick.listen((event) {
        event
          ..stopPropagation()
          ..preventDefault();
        _onRevokeApp(el);
      });
    });
  }

  Future _onRevokeApp(ButtonElement button) async {
    String clientId = button.dataset['client-id'];
    button.disabled = true;
    var loading = new Loading.small()
      ..renderAfter(button);
    try {
      await clientAppService.revoke(clientId);
      new FlashToast.success(i18n('success'), seconds:2);
      route.reload();
    } catch (e) {
      new Toast.error("$e").render();
    } finally {
      button.disabled = false;
      loading.remove();
    }
  }
}

