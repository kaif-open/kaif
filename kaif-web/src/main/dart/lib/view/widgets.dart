part of view;

class LargeCenterLoading {
  void renderInto(Element el) {
    trustInnerHtml(el,
    """
      <div class="large-center-loading">
        <i class="fa fa-cog fa-spin"></i>
      </div>
    """);
  }
}

class SmallLoading {
  void renderInto(Element el) {
    trustInnerHtml(el,
    """
      <i class="fa fa-cog fa-spin"></i>
    """);
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

    (window.document as HtmlDocument).body.nodes.add(el);
  }
}

class Toast {
  final String message;
  final Duration duration;
  String _type;

  Toast.error(this.message, this.duration) {
    _type = 'danger';
  }

  Toast.success(this.message, this.duration) {
    _type = 'success';
  }

  void render() {
    var el = trustHtml(
        """
     <div class="alert alert-${_type} toast">
       ${message}
     </div>
    """);

    (window.document as HtmlDocument).body.nodes.add(el);

    new Timer(duration, () {
      //TODO fade out
      el.remove();
    });
  }
}

class Alert {

  Element _elem;

  factory Alert.append(Element parent) {
    var alert = new Alert._();
    parent.append(alert._elem);
    return alert;
  }

  factory Alert.addInto(Element parent) {
    var alert = new Alert._();
    parent.nodes.add(alert._elem);
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