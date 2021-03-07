library forget_password_form;

import 'dart:html';

import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class ForgetPasswordForm {
  final Element elem;
  final AccountService accountService;
  late Alert alert;

  ForgetPasswordForm(this.elem, this.accountService) {
    elem.onSubmit.listen(_submit);
    alert = new Alert.append(elem);
  }

  void _submit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    TextInputElement nameInput =
        elem.querySelector('#nameInput') as TextInputElement;
    TextInputElement emailInput =
        elem.querySelector('#emailInput') as TextInputElement;
    ButtonElement submit = elem.querySelector('[type=submit]') as ButtonElement;
    submit.disabled = true;

    alert.hide();
    accountService
        .sendResetPassword(nameInput.value!, emailInput.value!)
        .then((_) {
      route.gotoSignInWithSendResetPasswordSuccess();
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      submit.disabled = false;
    });
  }
}
