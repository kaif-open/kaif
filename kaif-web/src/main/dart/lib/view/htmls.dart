part of view;

const UriPolicy NULL_URI_POLICY = const _NullUriPolicy();

class _NullUriPolicy implements UriPolicy {
  const _NullUriPolicy();

  bool allowsUri(String uri) {
    return true;
  }
}

const _ScriptLessValidator _SCRIPT_LESS_VALIDATOR = const _ScriptLessValidator();

class _ScriptLessValidator implements NodeValidator {

  const _ScriptLessValidator();

  bool allowsElement(Element element) {
    return element is! ScriptElement;
  }

  bool allowsAttribute(Element element, String attributeName, String value) {
    return !attributeName.toUpperCase().startsWith("ON");
  }
}


/**
 * parse html to create element, should only used in code template, not user generated content.
 *
 * note all <script> element and onFoo="" attribute will be removed
 */
Element _unSafeHtml(String rawHtml) {
  return new Element.html(rawHtml, validator:_SCRIPT_LESS_VALIDATOR);
}

/**
 * unsafe inner html, this is in library only and with carefully review.
 * do not use this method.
 *
 * note all <script> element and onFoo="" attribute will be removed
 */
void unSafeInnerHtml(Element parent, String rawInnerHtml) {
  parent.setInnerHtml(rawInnerHtml, validator:_SCRIPT_LESS_VALIDATOR);
}

void elementInsertAfter(Element sibling, Element elem) {
  var childNodes = sibling.parent.nodes;
  if (childNodes.isEmpty || childNodes.last == sibling) {
    childNodes.add(elem);
    return;
  }
  childNodes.insert(childNodes.indexOf(sibling) + 1, elem);
}