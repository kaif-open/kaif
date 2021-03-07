import 'dart:async';
import 'dart:html';

import 'package:kaif_web/comp/account/account_menu.dart';
import 'package:kaif_web/comp/account/account_settings.dart';
import 'package:kaif_web/comp/account/forget_password_form.dart';
import 'package:kaif_web/comp/account/granted_client_app.dart';
import 'package:kaif_web/comp/account/news_feed.dart';
import 'package:kaif_web/comp/account/reset_password_form.dart';
import 'package:kaif_web/comp/account/sign_in_form.dart';
import 'package:kaif_web/comp/account/sign_up_form.dart';
import 'package:kaif_web/comp/article/article-list.dart';
import 'package:kaif_web/comp/article/article_form.dart';
import 'package:kaif_web/comp/debate/debate_list.dart';
import 'package:kaif_web/comp/debate/debate_tree.dart';
import 'package:kaif_web/comp/developer/developer_client_app.dart';
import 'package:kaif_web/comp/oauth/oauth_authorize_form.dart';
import 'package:kaif_web/comp/server_part_loader.dart';
import 'package:kaif_web/comp/short_url.dart';
import 'package:kaif_web/comp/zone/zone_form.dart';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

final ServerType serverType = new ServerType();

//for dev server only:
customizeDev() {
  if (!serverType.isDevMode) {
    return;
  }
  querySelectorAll('#waitingPubServe').forEach((Element el) {
    el.text = 'Pub Serve Ready!';
    el.style.backgroundColor = '#006600';
    new Timer(const Duration(seconds: 1), () => el.remove());
  });
}

class AppModule {
  late AccountDao accountDao;
  late NewsFeedDao newsFeedDao;
  late AccountSession accountSession;
  late AccountService accountService;
  late ArticleService articleService;
  late ZoneService zoneService;
  late VoteService voteService;
  late ClientAppService clientAppService;
  late PartService partService;
  late ServerPartLoader serverPartLoader;
  late NewsFeedNotification newsFeedNotification;

  AppModule() {
    accountDao = new AccountDao();
    newsFeedDao = new NewsFeedDao();
    accountSession = new AccountSession(accountDao);

    var accessTokenProvider = accountSession.provideAccessToken;
    accountService = new AccountService(serverType, accessTokenProvider);
    articleService = new ArticleService(serverType, accessTokenProvider);
    voteService = new VoteService(serverType, accessTokenProvider);
    clientAppService = new ClientAppService(serverType, accessTokenProvider);
    zoneService = new ZoneService(serverType, accessTokenProvider);
    partService = new PartService(serverType, accessTokenProvider);
    newsFeedNotification =
        new NewsFeedNotification(accountService, accountSession, newsFeedDao);
    serverPartLoader = new ServerPartLoader(partService, _initializeComponents);
  }

  void _initializeComponents(dynamic parent) {
    // flash scope message if any, note that we show flash when:
    // 1. page loaded or
    // 2. part-template loaded
    FlashToast.flashIfAny();

    //TODO group in url module
    parent.querySelectorAll('[sign-up-form]').forEach((el) {
      new SignUpForm(el, accountService);
    });
    parent.querySelectorAll('[sign-in-form]').forEach((el) {
      new SignInForm(el, accountService, accountSession);
    });
    parent.querySelectorAll('[account-settings]').forEach((el) {
      new AccountSettings(el, accountService, accountSession);
    });
    parent.querySelectorAll('[forget-password-form]').forEach((el) {
      new ForgetPasswordForm(el, accountService);
    });
    parent.querySelectorAll('[reset-password-form]').forEach((el) {
      new ResetPasswordForm(el, accountService, accountSession);
    });
    parent.querySelectorAll('[article-form]').forEach((el) {
      new ArticleForm(el, articleService, accountSession);
    });
    parent.querySelectorAll('[debate-tree]').forEach((el) {
      new DebateTree(el, articleService, voteService, accountSession);
    });
    parent.querySelectorAll('[debate-list]').forEach((el) {
      new DebateList(
          el, articleService, voteService, accountSession, serverPartLoader);
    });
    parent.querySelectorAll('[article-list]').forEach((el) {
      new ArticleList(
          el, articleService, voteService, accountSession, serverPartLoader);
    });
    parent.querySelectorAll('[news-feed]').forEach((el) {
      new NewsFeedComp(el, serverPartLoader, newsFeedNotification);
    });
    parent.querySelectorAll('[short-url-input]').forEach((el) {
      new ShortUrlInput(el);
    });
    parent.querySelectorAll('[oauth-authorize-form]').forEach((el) {
      new OauthAuthorizeForm(el, accountSession, accountService);
    });
    parent.querySelectorAll('[developer-client-app]').forEach((el) {
      new DeveloperClientApp(el, clientAppService);
    });
    parent.querySelectorAll('[granted-client-app]').forEach((el) {
      new GrantedClientApp(el, clientAppService);
    });

    parent.querySelectorAll('[zone-form]').forEach((el) {
      new ZoneForm(el, zoneService, accountSession);
    });
  }

  void start() {
    //AccountMenu is singleton, it is not part of other components
    new AccountMenu(
        querySelector('[account-menu]')!, accountSession, newsFeedNotification);

    // apply to whole page
    _initializeComponents(window.document);

    // if server page use part-template.ftl, auto load
    serverPartLoader.tryLoadInto(
        '#__part_template', route.currentPartTemplatePath(),
        loading: new Loading.largeCenter());
  }
}

main() {
  initializeI18n(serverType.locale).then((locale) {
    new AppModule().start();
  });
  customizeDev();
}
