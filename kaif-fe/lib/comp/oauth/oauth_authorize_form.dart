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
  ButtonElement grantSubmit;
  ButtonElement denySubmit;
  StreamSubscription onGrantSubmitSubscription;
  Alert alert;

  OauthAuthorizeForm(this.elem, this.accountSession, this.accountService) {
    alert = new Alert.append(elem);
    nameInput = elem.querySelector('#nameInput');
    passwordInput = elem.querySelector('#passwordInput');
    grantSubmit = elem.querySelector('#grantSubmit');
    if (accountSession.isSignIn) {
      _initSignedIn();
    } else {
      elem.querySelector('[password-group]').classes.remove('hidden');
      onGrantSubmitSubscription = elem.onSubmit.listen(_authenticateSubmit);
    }

    denySubmit = elem.querySelector('#denySubmit')
      ..onClick.listen(_onDeny);
  }

  _initSignedIn() async {
    nameInput
      ..readOnly = true
      ..value = accountSession.current.username;
    passwordInput.remove();
    grantSubmit.disabled = true;
    try {
      await _prepareToken();
    } catch (requireCitizenException) {
      alert.renderError("$requireCitizenException");
      return;
    }
    grantSubmit.disabled = false;
    elem.onSubmit.listen((e) {
      grantSubmit.disabled = true;
      denySubmit.disabled = true;
      new Loading.small().renderAfter(grantSubmit);
    });
  }

  Future _onDeny(Event e) async {
    grantSubmit.disabled = true;
    denySubmit.disabled = true;
    (elem.querySelector('[name=grantDeny]') as HiddenInputElement).value = "true";
    elem.submit();
  }

  _authenticateSubmit(Event e) async {
    e
      ..preventDefault()
      ..stopPropagation();
    alert.hide();
    grantSubmit.disabled = true;
    denySubmit.disabled = true;
    var loading = new Loading.small()
      ..renderAfter(grantSubmit);
    AccountAuth auth = null;
    try {
      auth = await accountService.authenticate(nameInput.value, passwordInput.value);
    } catch (error) {
      alert.renderError("$error");
      grantSubmit.disabled = false;
      denySubmit.disabled = false;
      loading.remove();
      return;
    }
    accountSession.save(auth, rememberMe:false);
    onGrantSubmitSubscription.cancel();
    try {
      await _prepareToken();
    } catch (requireCitizenException) {
      alert.renderError("$requireCitizenException");
      denySubmit.disabled = false;
      loading.remove();
      return;
    }
    elem.submit();
  }

  Future _prepareToken() async {
    String token = await accountService.createOauthDirectAuthorizeToken();
    (elem.querySelector('[name=oauthDirectAuthorize]') as HiddenInputElement).value = token;
  }

}


