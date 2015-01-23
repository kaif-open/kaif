library i18n;
import 'dart:async';
import "package:intl/intl_browser.dart";
import "package:intl/intl.dart";
import "package:kaif_web/intl/lookup.dart";

class I18n {
  static Future<String> initialize() {
    return findSystemLocale().then((locale) {
      return initializeMessages(locale).then((_) => locale);
    });
  }

  static String key(String name, [List args = const []]) {
    return Intl.message('!${name}!', name: name, args: args);
  }
}