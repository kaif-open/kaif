library forget_password_form;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class ForgetPasswordForm {
  final Element elem;
  final AccountService accountService;

  ForgetPasswordForm(this.elem, this.accountService) {
    elem.onSubmit.listen(_submit);
  }

  void _submit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    TextInputElement nameInput = elem.querySelector('#nameInput');
    TextInputElement emailInput = elem.querySelector('#emailInput');
    Element alert = elem.querySelector('.alert');
    alert.classes.add('hidden');
    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    accountService.resetPassword(nameInput.value, emailInput.value)
    .then((_) {
      route.gotoSignInWithSendResetPasswordSuccess();
    }).catchError((e) {
      alert
        ..text = '${e}'
        ..classes.remove('hidden');

    }).whenComplete(() {
      submit.disabled = false;
    });
  }

}