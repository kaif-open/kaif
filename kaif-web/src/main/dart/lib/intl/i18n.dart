library i18n;
import 'dart:async';
import "package:intl/intl_browser.dart";
import "package:intl/intl.dart";
import "package:kaif_web/intl/lookup.dart";

class I18n {
  static Future<String> initialize(String serverLocale) {
    if (serverLocale != null) {
      Intl.systemLocale = Intl.canonicalizedLocale(serverLocale);
      return initializeMessages(serverLocale).then((_) => serverLocale);
    }
    return findSystemLocale().then((clientLocale) {
      return initializeMessages(clientLocale).then((_) => clientLocale);
    });
  }

  static String key(String name, [List args = const []]) {
    return Intl.message('!${name}!', name: name, args: args);
  }
}