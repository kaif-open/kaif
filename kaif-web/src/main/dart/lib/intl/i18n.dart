library i18n;

import 'dart:async';
import "package:intl/intl_browser.dart";
import "package:intl/intl.dart";
import "package:kaif_web/intl/lookup.dart";

/**
 * call once when app start
 */
Future<String> initializeI18n(String serverLocale) {
  if (serverLocale != null) {
    Intl.systemLocale = Intl.canonicalizedLocale(serverLocale);
    return initializeMessages(serverLocale).then((_) => serverLocale);
  }
  return findSystemLocale().then((clientLocale) {
    return initializeMessages(clientLocale).then((_) => clientLocale);
  });
}

String i18n(String name, [List args = const[]]) {
  return Intl.message('!${name}!', name: name, args: args);
}
