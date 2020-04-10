library forget_password_form;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class ForgetPasswordForm {
  final Element elem;
  final AccountService accountService;
  Alert alert;

  ForgetPasswordForm(this.elem, this.accountService) {
    elem.onSubmit.listen(_submit);
    alert = new Alert.append(elem);
  }

  void _submit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    TextInputElement nameInput = elem.querySelector('#nameInput');
    TextInputElement emailInput = elem.querySelector('#emailInput');
    ButtonElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    alert.hide();
    accountService.sendResetPassword(nameInput.value, emailInput.value)
    .then((_) {
      route.gotoSignInWithSendResetPasswordSuccess();
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      submit.disabled = false;
    });
  }

}