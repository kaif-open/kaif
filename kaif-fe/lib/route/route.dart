library route;

import 'dart:html';

const Router route = const Router._();

class Router {
  const Router._();

  String get signUp => '/account/sign-up';

  String get settings => '/account/settings';

  String get signOut => '/account/sign-out';

  String get signIn => '/account/sign-in';

  String get debateReplies => '/account/debate-replies';

  String get newsFeed => '/account/news-feed';

  String get home => '/';

  String get currentHash => window.location.hash;

  String user(String username) => '/u/$username';

  void reload({String? hash}) {
    if (hash != null) {
      window.location.hash = hash;
    }
    window.location.reload();
  }

  void gotoHome([String? queryString]) => _gotoWithQuery(home, queryString);

  void gotoSignInWithSignUpSuccess() =>
      _gotoWithQuery(signIn, 'sign-up-success');

  void gotoSignInWithSendResetPasswordSuccess() =>
      _gotoWithQuery(signIn, 'send-reset-password-success');

  void gotoSignInWithUpdatePasswordSuccess() =>
      _gotoWithQuery(signIn, 'update-password-success');

  void gotoNewArticlesOfZone(String zone) {
    _gotoWithQuery('/z/${zone}/new', null);
  }

  String shortArticleUrl(String articleId) {
    return "/d/$articleId";
  }

  /**
   * change page to target path, with optional query string.
   * the change will be history of browser
   *
   * browser do not send referrer.
   */
  void _gotoWithQuery(String path, String? queryString) {
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

  String? currentZone() {
    //see Zone.java for pattern
    var matches = new RegExp(r'/z/([a-z0-9][a-z0-9\-]{1,28}[a-z0-9])')
        .allMatches(window.location.pathname ?? "");
    return matches.isEmpty ? null : matches.first.group(1);
  }

  void redirect(String location) {
    window.location.href = location;
  }
}
