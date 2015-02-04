library comp_template;
import 'dart:html';

/**
 *
 * component template is rendered by server (invisible to user), it is use to create multiple
 * dart view component programmatically.
 *
 * you should not put any sensitive information on component template.
 * for sensitive information use ajax json or server_part_loader.dart instead.
 *
 * in server side ftl, the template should like:
 *
 * ```
 * <foo class="hidden" comp-template="my-name">
 *   <div.... etc
 * </foo>
 * ```
 *
 * you should include `class="hidden"` and `comp-template=".."` two attributes
 *
 * and in dart side, you can create `final` template instance in library scope of that component class:
 *
 * ```
 * final ComponentTemplate _myNameTemplate = new ComponentTemplate.take('my-name');
 *
 * class MyNameCompo {
 *   ...
 * }
 * ```
 *
 * library scope final is evaluated lazily and only create once.
 */
class ComponentTemplate {
  final String name;
  Element _templateElem;

  /**
   * `take` the template element in server rendered html. the template element on page will be
   * remove after load/parse (prevent end user to hack that html)
   *
   * so you should create ComponentTemplate only once (singleton and keep it) and as soon as page
   * loaded
   */
  ComponentTemplate.take(this.name) {
    Element template = querySelector('[comp-template="$name"]');
    _templateElem = (template.clone(true) as Element)
      ..classes.remove('hidden')
      ..attributes.remove('comp-template');

    //destroy the template to prevent user hacking
    template.remove();
  }

  Element createElement() {
    return _templateElem.clone(true);
  }
}