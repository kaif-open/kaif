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
      unSafeInnerHtml(elem, """
          <li class="pure-menu-can-have-children">
              <a class="pure-menu-label" href="#" username></a>
              <ul class="pure-menu-children menu-right" child-menu>
              </ul >
          </li>
      """);

      elem.nodes.insert(0, _menuElement(route.newsFeed, counterNotification.elem));

      elem.querySelector('[username]')
        ..text = auth.username;

      elem.querySelector('[child-menu]')
        ..append(_menuLink(route.user(auth.username), route.user(auth.username)))
        ..append(_menuLink(route.settings, i18n('account-menu.settings')))
        ..appendHtml(_menuSeparator)
        ..append(_createSignOut());

      _enablePureCssDropDown(elem);
    }
  }

  /*
     only support one level drop down, purecss-0.5
   */
  void _enablePureCssDropDown(Element parent) {
    var dropDownTarget = parent.querySelector('.pure-menu-can-have-children');
    var dropDownMenu = dropDownTarget.querySelector('.pure-menu-label');
    dropDownMenu.onClick.listen((e) {
      e
        ..preventDefault()
        ..stopPropagation();
      dropDownTarget.classes.toggle('pure-menu-open');
    });
    // cancel drop down menu if click outside
    document.documentElement.onClick.listen((
        e) => dropDownTarget.classes.toggle('pure-menu-open', false));
  }

  String get _menuSeparator => '<li class="pure-menu-separator"></li>';

  Element _menuLink(String href, String text) {
    //use .text = value to ensure safe html
    return new LIElement()
      ..append(new AnchorElement()
      ..href = href
      ..text = text) ;
  }

  Element _menuElement(String href, Element body) {
    return new LIElement()
      ..append(new AnchorElement()
      ..href = href
      ..append(body)) ;
  }

  final Element elem;
  final AccountSession accountSession ;

  final NewsFeedNotification newsFeedNotification;
  CounterNotification counterNotification;

  AccountMenu(this.elem, this.accountSession, this.newsFeedNotification) {
    counterNotification = new CounterNotification();
    var localAccount = accountSession.current;
    _render(localAccount);

    accountSession.extendsTokenIfRequired().then((extended) {
      if (extended) {
        //refresh render?
      }
    }).catchError((permissionError) {
      // should means invalid token
      accountSession.signOut();
      route.reload();
    });

    newsFeedNotification.onUnreadChanged.listen((value) {
      counterNotification.counter = value;
    });

    newsFeedNotification.getUnread().then((value) {
      counterNotification.counter = value;
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

class CounterNotification {
  Element elem;
  Element textElem;
  Element counterElem;

  CounterNotification() {
    elem = new SpanElement()
      ..classes.add("notification");

    //use .text = value to ensure safe html
    textElem = new SpanElement()
      ..classes.add("notification-name")
      ..text = i18n('account-menu.news-feed');

    counterElem = new SpanElement()
      ..classes.add('notification-counter');

    elem
      ..append(textElem)
      ..append(counterElem);

    counter = 0;

  }

  set counter(int value) {
    counterElem.classes.toggle('hidden', value <= 0);
    counterElem.text = value > 10 ? '10+' : value.toString();
  }

}