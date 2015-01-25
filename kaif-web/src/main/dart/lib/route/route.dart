library route;
import 'dart:html';

const Router route = const Router._();

class Router {

  const Router._();

  String get signUp => '/account/sign-up';

  String get settings => '/account/settings';

  String get signOut => '/account/sign-out';

  String get signIn => '/account/sign-in';

  String get home => '/' ;

  reload() => window.location.href = window.location.href;

  gotoHome([String queryString]) => _gotoWithQuery(home, queryString);

  gotoSignInWithSignUpSuccess() => _gotoWithQuery(signIn, 'sign-up-success');

  void _gotoWithQuery(String path, String queryString) {
    if (queryString == null) {
      window.location.href = path;
      return;
    }
    window.location.href = "${path}?${queryString}";
  }

  // change /account/foo to /account/foo.part
  String currentPartTemplatePath() {
    return '${window.location.pathname}.part';
  }
}