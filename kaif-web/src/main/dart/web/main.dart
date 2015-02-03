import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/comp/account/sign_up_form.dart';
import 'package:kaif_web/comp/account/sign_in_form.dart';
import 'package:kaif_web/comp/account/forget_password_form.dart';
import 'package:kaif_web/comp/account/reset_password_form.dart';
import 'package:kaif_web/comp/account/account_menu.dart';
import 'package:kaif_web/comp/account/account_settings.dart';
import 'package:kaif_web/comp/article/external_link_article_form.dart';
import 'package:kaif_web/comp/debate/debate_form.dart';
import 'package:kaif_web/comp/debate/debate_tree.dart';
import 'package:kaif_web/comp/server_part_loader.dart';
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

class AppModule {
  AccountDao accountDao;
  AccountSession accountSession;
  AccountService accountService;
  ArticleService articleService;
  PartService partService;
  ServerPartLoader serverPartLoader;

  AppModule() {
    accountDao = new AccountDao();
    accountSession = new AccountSession(accountDao);

    var accessTokenProvider = accountSession.provideAccessToken;
    accountService = new AccountService(serverType, accessTokenProvider);
    articleService = new ArticleService(serverType, accessTokenProvider);
    partService = new PartService(serverType, accessTokenProvider);

    serverPartLoader = new ServerPartLoader(partService, _initializeComponents);
  }

  void _initializeComponents(dynamic parent) {
    parent.querySelectorAll('[sign-up-form]').forEach((el) {
      new SignUpForm(el, accountService);
    });
    parent.querySelectorAll('[sign-in-form]').forEach((el) {
      if (accountSession.current != null) {
        route.gotoHome();
        return;
      }
      new SignInForm(el, accountService, accountSession);
    });
    parent.querySelectorAll('[account-settings]').forEach((el) {
      new AccountSettings(el, accountService, accountSession);
    });
    parent.querySelectorAll('[forget-password-form]').forEach((el) {
      new ForgetPasswordForm(el, accountService);
    });
    parent.querySelectorAll('[reset-password-form]').forEach((el) {
      new ResetPasswordForm(el, accountService);
    });
    parent.querySelectorAll('[external-link-article-form]').forEach((el) {
      new ExternalLinkArticleForm(el, articleService);
    });
    parent.querySelectorAll('[debate-tree]').forEach((el) {
      new DebateTree(el, articleService);
    });
  }

  void start() {
    //AccountMenu is singleton, it is not part of other components
    new AccountMenu(querySelector('[account-menu]'), accountSession);

    // apply to whole page
    _initializeComponents(window.document);

    // if server page use part-template.ftl, auto load
    serverPartLoader.tryLoadInto(
        '#__part_template',
        route.currentPartTemplatePath(),
        loading:new Loading.largeCenter());
  }
}

main() {
  initializeI18n(serverType.locale).then((locale) {
    new AppModule().start();
  });
  customizeDev();
}

