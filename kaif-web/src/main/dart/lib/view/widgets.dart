part of view;

class Loading {

  Element _el;

  factory Loading.none() {
    return const _NoneLoading();
  }

  Loading.small() {
    _el = trustHtml("""
      <i class="fa fa-cog fa-spin"></i>
    """);
  }

  Loading.largeCenter() {
    _el = trustHtml("""
      <div class="large-center-loading">
        <i class="fa fa-cog fa-spin"></i>
      </div>
    """);
  }

  void renderAfter(Element sibling) {
    sibling.parent.append(_el);
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
    var el = trustHtml(
        """
      <div class="large-error-modal">
         <div class="alert alert-danger">
           ${message}
           <p><a href="/">Home</a></p>
         </div>
      </div>
    """);

    (window.document as HtmlDocument).body.append(el);
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
    var el = trustHtml(
        """
     <div class="alert alert-${_type} toast">
       ${message}
     </div>
    """);

    (window.document as HtmlDocument).body.append(el);

    return new Future.delayed(_duration, () {
      el.remove();
    });
  }
}

class Alert {

  Element _elem;

  factory Alert.append(Element sibling) {
    var alert = new Alert._();
    sibling.append(alert._elem);
    return alert;
  }

  factory Alert.after(Element sibling) {
    var alert = new Alert._();
    sibling.parent.append(alert._elem);
    return alert;
  }

  Alert._() {
    _elem = new DivElement()
      ..classes.add('alert');
    hide();
  }

  void renderError(String message) => _render(message, 'danger');

  void renderInfo(String message) => _render(message, 'info');

  void renderSuccess(String message) => _render(message, 'success');

  void renderWarning(String message) => _render(message, 'warning');

  void _render(String message, String type) {
    _elem
      ..text = message
      ..classes.toggle('hide', false)
      ..classes.toggle('alert-danger', type == 'danger')
      ..classes.toggle('alert-info', type == 'info')
      ..classes.toggle('alert-success', type == 'success')
      ..classes.toggle('alert-warning', type == 'warning');
  }

  void hide() {
    _elem.classes.toggle('hide', true);
  }
}