library account_menu;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class AccountMenu {

  void _render(AccountAuth auth) {
    elem.nodes.clear();
    if (auth == null) {
      //TODO change to safe html approach, do not use trustHtml
      elem.nodes.add(trustHtml('<li><a href="${route.signIn}">${i18n('account-menu.sign-in')}</a></li>'));
      elem.nodes.add(trustHtml('<li><a href="${route.signUp}">${i18n('account-menu.sign-up')}</a></li>'));
    } else {
      elem.nodes.add(trustHtml('<li><a href="${route.settings}">${auth.username}</a></li>'));
      elem.nodes.add(_createSignOut());
    }
  }

  final Element elem;
  final AccountSession accountSession ;

  AccountMenu(this.elem, this.accountSession) {
    var localAccount = accountSession.current;
    _render(localAccount);

    accountSession.extendsTokenIfRequired().then((extended) {
      if (extended) {
        //refresh render?
      }
    }).catchError((permissionError) {
      // should means invalid token
      // TODO show error ?
      accountSession.signOut();
      route.reload();
    });
  }

  Element _createSignOut() {
    var signOut = trustHtml('<li><a href="#">${i18n('account-menu.sign-out')}</a></li>');
    signOut.querySelector('a').onClick.first.then((e) {
      e
        ..preventDefault()
        ..stopImmediatePropagation();
      accountSession.signOut();
      route.gotoHome();
    });
    return signOut;
  }

}