library sign_up_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

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
      if (!namePattern.hasMatch(partial)) {
        _showHint(i18n('sign-up.invalid_name'), ok:false);
        return;
      }
      accountService.isNameAvailable(partial).then((available) {
        String hintText = available ? 'sign-up.available' : 'sign-up.name_already_taken';
        _showHint(i18n(hintText), ok:available);
      });
    });

    elem.querySelector('#consentInput').onClick.first.then((e) {
      e.target.disabled = true;
      elem.querySelector('#consentLabel').text = 'X的！這個站還沒有條款，你同意什麼！';
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
      route.gotoSignInWithSignUpSuccess();
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
      alert
        ..classes.remove('hidden')
        ..text = i18n('sign-up.password_not_same');
      return;
    }

    accountService.isEmailAvailable(emailInput.value.trim()).then((available) {
      if (available) {
        _createAccount(loading, passwordInput, alert);
      } else {
        alert
          ..classes.remove('hidden')
          ..text = i18n('sign-up.email_already_taken');
      }
    });

  }

}