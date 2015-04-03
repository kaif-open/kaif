library oauth_authorize_form;
import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';
import 'dart:async';

class OauthAuthorizeForm {
  final FormElement elem;
  final AccountSession accountSession;
  final AccountService accountService;

  TextInputElement nameInput;
  PasswordInputElement passwordInput;
  ButtonElement submit;
  StreamSubscription onSubmitSubscription;
  Alert alert;

  OauthAuthorizeForm(this.elem, this.accountSession, this.accountService) {
    alert = new Alert.append(elem);
    nameInput = elem.querySelector('#nameInput');
    passwordInput = elem.querySelector('#passwordInput');
    submit = elem.querySelector('[type=submit]');
    if (accountSession.isSignIn) {
      nameInput
        ..readOnly = true
        ..value = accountSession.current.username;
      passwordInput.remove();
      submit.disabled = true;
      _prepareToken();
      submit.disabled = false;
      elem.onSubmit.listen((e) {
        submit.disabled = true;
        new Loading.small().renderAfter(submit);
      });
    } else {
      elem.querySelector('[password-group]').classes.remove('hidden');
      onSubmitSubscription = elem.onSubmit.listen(_authenticateSubmit);
    }
  }

  Future _authenticateSubmit(Event e) async {
    e
      ..preventDefault()
      ..stopPropagation();
    alert.hide();
    submit.disabled = true;
    var loading = new Loading.small()
      ..renderAfter(submit);
    AccountAuth auth = null;
    try {
      auth = await accountService.authenticate(nameInput.value, passwordInput.value);
    } catch (error) {
      alert.renderError("$error");
      submit.disabled = false;
      loading.remove();
      return;
    }
    accountSession.save(auth, rememberMe:false);
    onSubmitSubscription.cancel();
    await _prepareToken();
    elem.submit();
  }

  Future _prepareToken() async {
    String token = await accountService.createOauthDirectAuthorizeToken();
    (elem.querySelector('[name=OAUTH_DIRECT_AUTHORIZE]') as HiddenInputElement).value = token;
  }

}


