import 'package:kaif_web/model.dart';
import 'package:kaif_web/service/service.dart';
import 'package:kaif_web/intl/i18n.dart';
import 'package:kaif_web/comp/account/sign_up_form.dart';
import 'package:kaif_web/comp/account/login_form.dart';
import 'dart:html';
import 'dart:async';

//for dev server only:
customizeDev() {
  querySelectorAll('#waitingPubServe').forEach((Element el) {
    el.text = 'Pub Serve Ready!';
    el.style.backgroundColor = '#006600';
    new Timer(const Duration(seconds:1), () => el.remove());
  });
}

appStart(String locale) {
  var serverType = new ServerType();
  var accountService = new AccountService(serverType);
  var accountDao = new AccountDao();
  querySelectorAll('[sign-up-form-controller]').forEach((el) {
    new SignUpForm(el, accountService);
  });
  querySelectorAll('[login-form-controller]').forEach((el) {
    new LoginForm(el, accountService, accountDao);
  });
}

main() {
  I18n.initialize().then(appStart);
  customizeDev();
}