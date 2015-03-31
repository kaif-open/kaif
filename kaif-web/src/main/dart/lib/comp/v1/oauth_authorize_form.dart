library oauth_authorize_form;
import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';
import 'dart:async';

class OauthAuthorizeForm {
  final Element elem;
  final AccountSession accountSession;
  final V1OauthService v1OauthService;

  String state;
  String scope;
  String redirectUri;
  String clientId;

  TextInputElement nameInput;
  PasswordInputElement passwordInput;

  OauthAuthorizeForm(this.elem, this.accountSession, this.v1OauthService) {
    state = (elem.querySelector('[name=state]') as HiddenInputElement).value;
    scope = (elem.querySelector('[name=scope]') as HiddenInputElement).value;
    redirectUri = (elem.querySelector('[name=redirectUri]') as HiddenInputElement).value;
    clientId = (elem.querySelector('[name=clientId]') as HiddenInputElement).value;
    nameInput = elem.querySelector('#nameInput');
    passwordInput = elem.querySelector('#passwordInput');
    if (accountSession.isSignIn) {
      nameInput
        ..readOnly = true
        ..value = accountSession.current.username;
      passwordInput.remove();
      elem.onSubmit.listen(_onDirectAuthorize);
    } else {
      elem.querySelector('[password-group]').classes.remove('hidden');
      elem.onSubmit.listen(_onSignInAuthorize);
    }
  }

  Future _onDirectAuthorize(Event e) async {
    e
      ..preventDefault()
      ..stopPropagation();

    String location = await v1OauthService.directAuthorize(clientId, scope, redirectUri, state);
    route.redirect(location);
  }

  Future _onSignInAuthorize(Event e) async {
    e
      ..preventDefault()
      ..stopPropagation();
    String location = await v1OauthService.signInAuthorize(
        nameInput.value, passwordInput.value,
        clientId, scope, redirectUri, state);
    route.redirect(location);
  }
}


