part of view;

const UriPolicy NULL_URI_POLICY = const _NullUriPolicy();

class _NullUriPolicy implements UriPolicy {
  const _NullUriPolicy();
  bool allowsUri(String uri) {
    return true;
  }
}

const _NullTreeSanitizer _NULL_TREE_SANITIZER = const _NullTreeSanitizer();

class _NullTreeSanitizer implements NodeTreeSanitizer {
  const _NullTreeSanitizer();
  void sanitizeTree(Node node) {}
}

/**
 * parse html to create element, should only used in code template, not user generated content.
 */
Element trustHtml(String rawHtml) {
  return new Element.html(rawHtml, treeSanitizer:_NULL_TREE_SANITIZER);
}

void trustInnerHtml(Element parent, String rawInnerHtml) {
  parent.setInnerHtml(rawInnerHtml, treeSanitizer:_NULL_TREE_SANITIZER);
}