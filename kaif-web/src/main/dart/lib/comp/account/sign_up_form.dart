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
    Element alert = elem.querySelector('.alert');
    Element loading = elem.querySelector('.loading');

    alert.classes.add('hidden');
    alert.text = '';

    if (passwordInput.value != confirmPasswordInput.value) {
      //TODO i18n
      alert
        ..classes.remove('hidden')
        ..text = 'password not the same';
      return;
    }

    loading.classes.remove('hidden');
    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    accountService.createAccount(nameInput.value, emailInput.value, passwordInput.value)//
    .then((_) {
      window.location.href = '/login?sign-up-success';
    }).catchError((e) {
      alert
        ..classes.remove('hidden')
        ..text = '${e}';
    }).whenComplete(() {
      submit.disabled = false;
      loading.classes.add('hidden');
    });

  }

}