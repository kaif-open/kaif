library sign_up_form;

import 'dart:html';
import 'package:kaif_web/service/service.dart';

class SignUpForm {
  final Element elem;
  final AccountService accountService;
  TextInputElement nameInput;
  TextInputElement emailInput;

  SignUpForm(this.elem, this.accountService) {
    nameInput = elem.querySelector('#nameInput');
    emailInput = elem.querySelector('#emailInput');
    elem.onSubmit.listen(_signUp);
    //pattern follow Account#NAME_PATTERN
    var namePattern = new RegExp(r'^[a-zA-Z_0-9]{3,15}$');

    nameInput.onKeyUp.map((e) => nameInput.value.trim()).listen((partial) {
      //TODO i18n
      if (!namePattern.hasMatch(partial)) {
        _showHint('Invalid name', ok:false);
        return;
      }
      accountService.isNameAvailable(partial).then((available) {
        String hintText = available ? '&#10003; Available' : 'Already taken!';
        _showHint(hintText, ok:available);
      });
    });
  }

  void _showHint(String hintText, {bool ok}) {
    var hint = elem.querySelector('.nameHint');
    hint
      ..classes.toggle('text-success', ok)
      ..classes.toggle('text-danger', !ok)
      ..innerHtml = hintText;
  }

  void _createAccount(Element loading, TextInputElement passwordInput, Element alert) {
    loading.classes.remove('hidden');
    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    accountService.createAccount(nameInput.value, emailInput.value, passwordInput.value)
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

  void _signUp(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

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
        ..text = 'Passwords are not the same';
      return;
    }

    accountService.isEmailAvailable(emailInput.value.trim()).then((available) {
      if (available) {
        _createAccount(loading, passwordInput, alert);
      } else {
        //TODO i18n
        alert
          ..classes.remove('hidden')
          ..text = 'Email is already taken';
      }
    });

  }

}