library account_settings;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class AccountSettings {

  final Element elem;
  final AccountService accountService;

  AccountSettings(this.elem, this.accountService) {
    _createReactivateIfRequire();
  }

  void _createReactivateIfRequire() {
    ButtonElement found = elem.querySelector('#account-reactivate');
    if (found == null) {
      //already activated
      return;
    }

    found.onClick.first.then((e) {
      e
        ..preventDefault()
        ..stopPropagation();
      found.disabled = true;
      accountService.resendActivation().then((_) {
        new Toast.success(i18n('account-settings.reactivation_sent'),
        const Duration(seconds:5)).render();
      }).catchError((e) {
        new Toast.error(e.toString(), const Duration(seconds:10)).render();
      });
    });
  }
}