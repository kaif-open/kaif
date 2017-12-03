library kaif_web_transformer;

/**
 * transformer convert spring messages to dart translation:
 *
 * lib/i18n_link/messages_foo.properties => lib/intl/messages_foo.dart
 *
 * note that:
 *
 *  1. original messages_foo.dart will be replaced
 *  2. `i18n_link` is symbolic link to spring's messages folder
 *
 */
import 'package:barback/barback.dart';
import 'package:path/path.dart' as path;
import 'dart:async';

class ConvertSpringI18n extends Transformer {

  // A constructor named "asPlugin" is required. It can be empty, but
  // it must be present. It is how pub determines that you want this
  // class to be publicly available as a loadable transformer plugin.
  ConvertSpringI18n.asPlugin();

  String get allowedExtensions => ".properties .dart";

  Future apply(Transform transform) {
    if (transform.primaryInput.id.extension == '.dart') {
      //exclude original messages_foo.dart file
      transform.consumePrimary();
      return new Future.value(null);
    }

    return transform.primaryInput.readAsString().then((content) {

      var assetId = transform.primaryInput.id;
      var messages_locale = path.basenameWithoutExtension(assetId.path);

      /**
       * conversion rule:
       *
       * messages_foo.properties => locale: foo  => messages_foo.dart
       * messages.properties     => locale: en   => messages_en.dart
       */
      var locale = messages_locale.replaceAll(new RegExp('messages_?'), '');
      locale = locale.isEmpty ? 'en' : locale;

      String newPath = assetId.changeExtension('.dart').path
      .replaceAll('i18n_link', 'intl')
      .replaceAll('messages.dart', 'messages_en.dart');

      var newId = new AssetId(assetId.package, newPath);
      String newContent = resolveTemplate(locale, convertPropertiesToDart(content));
      transform.addOutput(new Asset.fromString(newId, newContent));

      //exclude messages_foo.properties
      transform.consumePrimary();
    });
  }
}

List<String> convertPropertiesToDart(String properties) {
  return properties.replaceAll(new RegExp("^#.*", multiLine:true), '')
  .split(new RegExp(r'[\n\r]+'))
  .where((line) {
    return line.trim() != '';
  }).map((line) {
    Match match = new RegExp("([^=]+)=(.+)").firstMatch(line);
    String key = match.group(1).trim();
    String value = match.group(2).trim();
    var translated = new Translation(value);
    return '"$key": $translated';
  });
}

/**
 * convert
 *
 *  `email.activation.greeting=foo, {0}`
 *
 * to
 *
 *  `(a0) => Intl.messages("foo, ${a0}");`
 */
class Translation {
  List<String> args;
  String message;

  Translation(String rawValue) {

    message = rawValue.replaceAllMapped(new RegExp('\{([0-9])\}'), (match) {
      var i = match.group(1);
      return r'${' + "a${i}" + r'}';
    });
    List<Match> matches = new RegExp('\{[0-9]\}').allMatches(rawValue).toList();
    args = matches.map((match) {
      var i = matches.indexOf(match);
      return "a$i";
    }).toList();
  }

  String toString() {
    String argPart = args.join(", ");
    return '($argPart) => Intl.message("$message")';
  }
}

String resolveTemplate(String locale, List<String> translates) {

  var translateLines = translates.join(',\n');
  return """
library message_${locale};

import 'package:intl/intl.dart';
import 'package:intl/message_lookup_by_library.dart';

MessageLookupByLibrary get messages => new MessageLookup();

class MessageLookup extends MessageLookupByLibrary {

  get localeName => '${locale}';

  final messages = {

${translateLines}

  };
}
""";

}
