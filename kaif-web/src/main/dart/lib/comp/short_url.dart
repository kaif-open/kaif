library short_url;
import 'dart:html';

class ShortUrlInput {
  ShortUrlInput(TextInputElement elem) {
    elem.onClick.listen((_) {
      elem.select();
    });
  }
}