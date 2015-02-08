part of view;

void _insertAfter(Element sibling, Element elem) {
  var childNodes = sibling.parent.nodes;
  if (childNodes.isEmpty || childNodes.last == sibling) {
    childNodes.add(elem);
    return;
  }
  childNodes.insert(childNodes.indexOf(sibling) + 1, elem);
}

class Loading {

  Element _el;

  factory Loading.none() {
    return const _NoneLoading();
  }

  Loading.small() {
    _el = _unSafeHtml("""
      <i class="fa fa-cog fa-spin"></i>
    """);
  }

  Loading.largeCenter() {
    _el = _unSafeHtml("""
      <div class="large-center-loading">
        <i class="fa fa-cog fa-spin"></i>
      </div>
    """);
  }

  void renderAfter(Element sibling) {
    _insertAfter(sibling, _el);
  }

  void renderAppend(Element parent) {
    parent.append(_el);
  }

  void remove() {
    _el.remove();
  }
}

class _NoneLoading implements Loading {

  void set _el(_) {
  }

  Element get _el => null;

  const _NoneLoading();

  void renderAfter(Element parent) {
  }

  void renderAppend(Element sibling) {
  }

  void remove() {
  }
}

class LargeErrorModal {
  final String message;

  LargeErrorModal(this.message);

  void render() {

    //do not embed ${message} within _unSafeHtml !
    var dangerUnSafeElem = _unSafeHtml(
        """
      <div class="large-error-modal">
         <div class="alert alert-danger">
           <span class="safeMessage"></span>
           <p><a href="/">Home</a></p>
         </div>
      </div>
    """);

    var safeHtmlElem = dangerUnSafeElem.querySelector('span.safeMessage');

    //must use `.text = message` to ensure html safety !!
    safeHtmlElem.text = message;
    (window.document as HtmlDocument).body.append(dangerUnSafeElem);
  }
}

/**
    material design snack bar
    http://www.google.com/design/spec/components/snackbars-toasts.html#

    snack bar is singleton
 */
class SnackBar {

  static SnackBar _INSTANCE = new SnackBar._();

  factory SnackBar(String message, {AnchorElement action}) {
    return _INSTANCE
      .._message = message
      .._actionElem = action;

  }

  String _message;
  Element _dangerUnSafeElem;
  Element _actionElem;
  Timer _timer;

  SnackBar._() {
    _dangerUnSafeElem = _unSafeHtml(
        """
       <div class="snack-bar alert alert-warning">
         <span class="safeMessage"></span>
         <span class="action"></span>
       </div>
    """);
  }

  void render({int seconds:100000000}) {
    var actionWrapper = _dangerUnSafeElem.querySelector('.action');
    actionWrapper.nodes.clear();
    if (_actionElem != null) {
      actionWrapper.append(_actionElem);
    }

    _dangerUnSafeElem.querySelector('.safeMessage').text = _message;

    //animation ?
    (window.document as HtmlDocument).body.append(_dangerUnSafeElem);

    if (_timer != null) {
      _timer.cancel();
    }
    _timer = new Timer(new Duration(seconds:seconds), () {
      _dangerUnSafeElem.remove();
      actionWrapper.nodes.clear();
      _timer = null;
    });
  }
}

class Toast {
  final String message;
  Duration _duration;
  String _type;

  Toast.error(this.message, {int seconds:10}) {
    _type = 'danger';
    _duration = new Duration(seconds:seconds);
  }

  Toast.success(this.message, {int seconds:5}) {
    _type = 'success';
    _duration = new Duration(seconds:seconds);
  }

  Future render() {
    var safeHtmlElem = new DivElement()
      ..classes.addAll(['alert', 'alert-${_type}', 'toast']);

    //.text = message to ensure safe html here !
    safeHtmlElem.text = message;
    (window.document as HtmlDocument).body.append(safeHtmlElem);

    return new Future.delayed(_duration, () {
      safeHtmlElem.remove();
    });
  }
}

class Alert {

  Element _safeHtmlElem;

  factory Alert.append(Element sibling) {
    var alert = new Alert._();
    sibling.append(alert._safeHtmlElem);
    return alert;
  }

  factory Alert.after(Element sibling) {
    var alert = new Alert._();

    _insertAfter(sibling, alert._safeHtmlElem);

    return alert;
  }

  Alert._() {
    _safeHtmlElem = new DivElement()
      ..classes.add('alert');
    hide();
  }

  void renderError(String message) => _render(message, 'danger');

  void renderInfo(String message) => _render(message, 'info');

  void renderSuccess(String message) => _render(message, 'success');

  void renderWarning(String message) => _render(message, 'warning');

  void _render(String message, String type) {

    // use .text = message ensure safe html
    _safeHtmlElem
      ..text = message
      ..classes.toggle('hidden', false)
      ..classes.toggle('alert-danger', type == 'danger')
      ..classes.toggle('alert-info', type == 'info')
      ..classes.toggle('alert-success', type == 'success')
      ..classes.toggle('alert-warning', type == 'warning');
  }

  void hide() {
    _safeHtmlElem.classes.toggle('hidden', true);
  }
}
