import 'package:kaif_web/model.dart';
import 'package:kaif_web/service/service.dart';
import 'package:kaif_web/comp/account/sign_up_form.dart';
import 'package:kaif_web/comp/account/login_form.dart';
import 'dart:html';

main() {
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