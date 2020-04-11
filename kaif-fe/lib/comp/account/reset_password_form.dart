library reset_password_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

class ResetPasswordForm {

  final Element elem;
  final AccountService accountService;
  final AccountSession accountSession;
  Alert alert;

  ResetPasswordForm(this.elem, this.accountService, this.accountSession) {
    alert = new Alert.append(elem);
    elem.onSubmit.listen(_onSubmit);
  }

  void _onSubmit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    TextInputElement passwordInput = elem.querySelector('#passwordInput');
    TextInputElement confirmPasswordInput = elem.querySelector('#confirmPasswordInput');
    alert.hide();

    if (passwordInput.value != confirmPasswordInput.value) {
      alert.renderError(i18n('sign-up.password-not-same'));
      return;
    }

    ButtonElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    var token = Uri.parse(window.location.href).queryParameters['key'];
    var loading = new Loading.small()
      ..renderAfter(submit);
    accountService.updatePasswordWithToken(token, passwordInput.value)
    .then((_) {
      // force sign out because old token is stale because password changed
      accountSession.signOut();
      route.gotoSignInWithUpdatePasswordSuccess();
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      submit.disabled = false;
      loading.remove();
    });

  }

}