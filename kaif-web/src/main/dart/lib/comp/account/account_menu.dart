library account_menu;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class AccountMenu {

  void _render(AccountAuth auth) {
    elem.nodes.clear();
    if (auth == null) {
      elem.append(_menuLink(route.signIn, i18n('account-menu.sign-in')));
      elem.append(_menuLink(route.signUp, i18n('account-menu.sign-up')));
    } else {
      elem.append(_menuLink(route.debateReplies, i18n('account-menu.debate-replies')));
      elem.append(_menuLink(route.settings, auth.username));
      elem.nodes.add(_createSignOut());
    }
  }

  Element _menuLink(String href, String text) {
    //use .text = value to ensure safe html
    return new LIElement()
      ..append(new AnchorElement()
      ..href = href
      ..text = text) ;
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
    var signOut = _menuLink("#", i18n('account-menu.sign-out'));
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