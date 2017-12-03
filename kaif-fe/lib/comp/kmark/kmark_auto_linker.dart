library kmark_auto_linker;
import 'dart:html';
import 'dart:math' as Math;
import 'package:kaif_web/util.dart';
import 'dart:async';

class KmarkAutoLinker {

  final TextAreaElement contentInput;

  static final RegExp _PURE_LINK_REGEX = new RegExp(r'^\s*(https?://[^\s]+)\s*$',
  caseSensitive:false);

  KmarkAutoLinker(this.contentInput) {
    //chrome/firefox/safari supported
    contentInput.onPaste.listen(_onPasted);
  }

  void _onPasted(Event e) {

    // print("onPasted range: ${contentInput.selectionStart}, ${contentInput.selectionEnd}");

    var initialText = contentInput.value;
    var pasteAtIndex = contentInput.selectionStart;
    //if paste without selection, length is 0
    var selectionLength = contentInput.selectionEnd - pasteAtIndex;
    var selectionText = initialText.substring(contentInput.selectionStart,
    contentInput.selectionEnd);

    // print("current: $initialText");
    new Timer(const Duration(milliseconds:1), () {
      var pastedTextLength = contentInput.value.length - (initialText.length - selectionLength);
      var end = pasteAtIndex + pastedTextLength;
      var pastedText = contentInput.value.substring(pasteAtIndex, end);
      _onTextPasted(selectionText, pastedText.trim());
    });

    // clipboardData only support in chrome, give up
    //    var items = e.clipboardData.items;
    //    for (var i = 0; i < items.length; i++) {
    //      var item = items[i];
    //      // debug
    //      print("type: ${item.type}, kind: ${item.kind}");
    //
    //      if (item.type == 'text/plain') {
    //        item.getAsString().then(_onTextPasted);
    //        break;
    //      }
    //    }
  }

  void _onTextPasted(String selectionText, String text) {
    // if user selected url and paste new link, we don't process
    // it because user just replace it's old link
    if (_PURE_LINK_REGEX.hasMatch(selectionText)) {
      return;
    }

    //detect only pure link text
    var match = _PURE_LINK_REGEX.firstMatch(text);
    if (match == null) {
      return;
    }
    var link = match.group(1);
    if (_isLinkWithinCodeBlock(contentInput.value, link)) {
      return;
    }

    if (_isLinkOnReferenceAppendix(contentInput.value, link)) {
      return;
    }

    _applyAutoLink(selectionText, link);
  }

  /**
   * two cases:
   *
   * 1. user manually editing appendix section, when he paste into this section,
   *    we should not process it
   * 2. user paste a link in textarea, but the link already in appendix, we ignore this case too.
   */
  bool _isLinkOnReferenceAppendix(String fullText, String link) {
    var existRefs = ReferenceAppendix.tryParse(fullText);
    return existRefs.any((ref) => ref.url == link);
  }

  /**
   * detect pasted link is within <code> or <pre>, we count number of `fence` syntax before link text.
   * if count is odd, it means user is editing within the block.
   *
   * The same rule apply to `code` syntax.
   */
  bool _isLinkWithinCodeBlock(String fullText, String link) {
    var position = fullText.indexOf(link);
    if (position < 0) {
      return false;
    }
    String textBeforeLink = fullText.substring(0, fullText.indexOf(link));
    const fence = '```';
    int fenceCount = fence.allMatches(textBeforeLink).length;
    print(fenceCount.toString());
    if (fenceCount % 2 == 1) {
      // odd number of fence, it means the link within fence block, so we won't trigger it;
      return true;
    }
    String textWithoutFence = textBeforeLink.replaceAll(fence, '');
    const code = '`';
    int codeCount = code.allMatches(textWithoutFence).length;
    if (codeCount % 2 == 1) {
      // same as fence rule
      return true;
    }
    return false;
  }

  void _applyAutoLink(String selectionText, String link) {
    // note that contentInput.value now include pasted text.

    // compute next appendix index
    var existRefs = ReferenceAppendix.tryParse(contentInput.value);
    // print("exist: $existRefs");
    int nextIndex = ReferenceAppendix.nextIndex(existRefs);

    // replace with placeholder and append appendix
    var placeholder = isStringBlank(selectionText)
                      ? i18n("kmark.auto-link-placeholder")
                      : selectionText.trim();
    var replacedText = " [$placeholder][$nextIndex] ";

    // note that we only search first occurrence of link, so if multiple link present.
    // the replace will be problematic.
    contentInput.value = contentInput.value.replaceFirst(link, replacedText);
    if (existRefs.isEmpty) {
      contentInput.value += "\n";
    }
    contentInput.value += "\n${new ReferenceAppendix(nextIndex.toString(), link)}";

    // apply placeholder selection:
    var start = contentInput.value.indexOf(replacedText) + 2; // 2 is `space+[`
    contentInput.selectionStart = start;
    contentInput.selectionEnd = start + placeholder.length;
  }
}

/**
 * the parser support index is not number, (because server allow non-number index).
 */
class ReferenceAppendix {
  static final RegExp _LINE_PATTERN = new RegExp(r'^\s*\[([^\]]+)\]:\s*(http.+)\s*$',
  multiLine:true);
  final String index;
  final String url;

  static int nextIndex(List<ReferenceAppendix> appendixes) {
    int maxNumericIndex = appendixes.map((apx) => apx.index).map((index) {
      return int.parse(index, onError:(s) => 0);
    }).fold(0, (left, right) => Math.max(left, right));

    int maxIndex = Math.max(maxNumericIndex, appendixes.length);
    return maxIndex + 1;
  }

  static List<ReferenceAppendix> tryParse(String rawLines) {
    if (rawLines == null) {
      return [];
    }
    return _LINE_PATTERN.allMatches(rawLines).map((match) {
      return new ReferenceAppendix(match.group(1), match.group(2));
    }).toList();
  }

  ReferenceAppendix(this.index, this.url);

  String toString() => "[$index]: $url";
}