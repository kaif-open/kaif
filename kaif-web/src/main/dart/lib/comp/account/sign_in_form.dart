library sign_in_form;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class SignInForm {
  final Element elem;
  final AccountService accountService;
  final AccountDao accountDao;

  SignInForm(this.elem, this.accountService, this.accountDao) {
    elem.onSubmit.listen(_login);
  }

  void _login(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    TextInputElement nameInput = elem.querySelector('#nameInput');
    TextInputElement passwordInput = elem.querySelector('#passwordInput');
    CheckboxInputElement rememberMeInput = elem.querySelector('#rememberMeInput');
    Element alert = elem.querySelector('.alert');
    alert.classes.add('hidden');
    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    accountService.authenticate(nameInput.value, passwordInput.value)//
    .then((AccountAuth accountAuth) {
      accountDao.saveAccount(accountAuth, rememberMe:rememberMeInput.checked);
      //TODO handle ?from=
      route.gotoHome();
    }).catchError((e) {
      alert
        ..text = i18n('sign-in.authentication_failed')
        ..classes.remove('hidden');

    }).whenComplete(() {
      submit.disabled = false;
    });

  }

}