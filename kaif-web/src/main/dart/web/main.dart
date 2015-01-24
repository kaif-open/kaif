import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/comp/account/sign_up_form.dart';
import 'package:kaif_web/comp/account/sign_in_form.dart';
import 'package:kaif_web/comp/account/account_menu.dart';
import 'dart:html';
import 'dart:async';

final ServerType serverType = new ServerType();

//for dev server only:
customizeDev() {
  if (!serverType.isDevMode) {
    return;
  }
  querySelectorAll('#waitingPubServe').forEach((Element el) {
    el.text = 'Pub Serve Ready!';
    el.style.backgroundColor = '#006600';
    new Timer(const Duration(seconds:1), () => el.remove());
  });
}

appStart(String locale) {
  var accountService = new AccountService(serverType);
  var accountDao = new AccountDao();

  new AccountMenu(querySelector('[account-menu]'), accountService, accountDao);
  querySelectorAll('[sign-up-form]').forEach((el) {
    new SignUpForm(el, accountService);
  });
  querySelectorAll('[sign-in-form]').forEach((el) {
    new SignInForm(el, accountService, accountDao);
  });
}

main() {
  initializeI18n(serverType.locale).then(appStart);
  customizeDev();
}