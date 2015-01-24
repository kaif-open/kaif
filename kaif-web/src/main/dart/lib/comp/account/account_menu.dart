library account_menu;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class AccountMenu {

  void _render(Account account) {
    elem.nodes.clear();
    if (account == null) {
      elem.nodes.add(trustHtml('<li><a href="${route.signIn}">${i18n('account-menu.sign_in')}</a></li>'));
      elem.nodes.add(trustHtml('<li><a href="${route.signUp}">${i18n('account-menu.sign_up')}</a></li>'));
    } else {
      elem.nodes.add(trustHtml('<li><a href="${route.settings}">${account.name}</a></li>'));
      elem.nodes.add(_createSignOut());
    }
  }

  final Element elem;
  final AccountService accountService;
  final AccountDao accountDao;

  AccountMenu(this.elem, this.accountService, this.accountDao) {
    var localAccount = accountDao.loadAccount();
    _render(localAccount);
  }

  Element _createSignOut() {
    var signOut = trustHtml('<li><a href="#">${i18n('account-menu.sign_out')}</a></li>');
    signOut.querySelector('a').onClick.first.then((e) {
      e
        ..preventDefault()
        ..stopImmediatePropagation();
      accountDao.removeAccount();
      route.gotoHome();
    });
    return signOut;
  }

}