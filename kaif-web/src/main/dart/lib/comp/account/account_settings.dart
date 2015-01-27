library account_settings;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class AccountSettings {

  final Element elem;
  final AccountService accountService;
  final AccountSession accountSession;

  AccountSettings(this.elem, this.accountService, this.accountSession) {
    _createReactivateIfRequire();

    new _UpdateNewPasswordForm(
        elem.querySelector('[update-new-password-form]'),
        accountService,
        accountSession);
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


class _UpdateNewPasswordForm {
  final Element elem;
  final AccountSession accountSession;
  final AccountService accountService;
  Alert alert;

  _UpdateNewPasswordForm(this.elem, this.accountService, this.accountSession) {
    alert = new Alert.append(elem);
    elem.onSubmit.listen(_onSubmit);
  }

  void _onSubmit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();
    TextInputElement oldPasswordInput = elem.querySelector('#oldPasswordInput');
    TextInputElement passwordInput = elem.querySelector('#passwordInput');
    TextInputElement confirmPasswordInput = elem.querySelector('#confirmPasswordInput');
    alert.hide();

    if (passwordInput.value != confirmPasswordInput.value) {
      alert.renderError(i18n('sign-up.password_not_same'));
      return;
    }

    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    var loading = new Loading.small()
      ..renderAfter(submit);
    accountService.updateNewPassword(oldPasswordInput.value, passwordInput.value)
    .then((AccountAuth auth) {
      accountSession.saveAccount(auth);
      new Toast.success(i18n('account-settings.update_new_password_success'),
      const Duration(seconds:3)).render();
      elem.remove();
      alert.hide();
      //TODO refresh after toast done
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      submit.disabled = false;
      loading.remove();
    });

  }
}