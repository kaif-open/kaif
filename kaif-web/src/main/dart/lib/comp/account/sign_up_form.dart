library sign_up_form;

import 'dart:html';
import 'package:kaif_web/service/service.dart';

class SignUpForm {
  final Element elem;
  final AccountService accountService;

  SignUpForm(this.elem, this.accountService) {
    elem.onSubmit.listen(_signUp);
  }

  void _signUp(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    TextInputElement nameInput = elem.querySelector('#nameInput');
    TextInputElement emailInput = elem.querySelector('#emailInput');
    TextInputElement passwordInput = elem.querySelector('#passwordInput');
    TextInputElement confirmPasswordInput = elem.querySelector('#confirmPasswordInput');

    //TODO validate input
    //TODO show loading
    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    accountService.createAccount(nameInput.value, emailInput.value, passwordInput.value)//
    .then((_) {
      window.location.href = '/login?sign-up-success';
    }).whenComplete(() => submit.disabled = false);

  }

}