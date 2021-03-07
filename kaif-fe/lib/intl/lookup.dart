/**
 * DO NOT EDIT. This is code generated via pkg/intl/generate_localized.dart
 * This is a library that looks up messages for specific locales by
 * delegating to the appropriate library.
 */

library lookup;

import 'dart:async';

import 'package:intl/message_lookup_by_library.dart';
import 'package:intl/src/intl_helpers.dart';

import 'messages_en.dart' as messages_en;
import 'messages_zh.dart' as messages_zh;

MessageLookupByLibrary _findExact(String? localeName) {
  if (localeName != null &&
      (localeName.startsWith('zh_') || localeName == 'zh')) {
    return messages_zh.messages;
  }
  return messages_en.messages;
}

/** User programs should call this before using [localeName] for messages.*/
Future initializeMessages(String localeName) {
  initializeInternalMessageLookup(() => new CompositeMessageLookup());
  messageLookup.addLocale(localeName, _findExact);
  return new Future.value(null);
}
