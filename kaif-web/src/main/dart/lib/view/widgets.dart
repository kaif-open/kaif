part of view;

class LargeCenterLoading {
  void renderInTo(Element el) {
    trustInnerHtml(el,
    """
      <div class="lg-center-loading">
        <i class="fa fa-cog fa-spin"></i>
      </div>
    """);
  }
}

class SmallLoading {
  void renderInTo(Element el) {
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

  Toast.error(this.message, this.duration);

  void render() {
    var el = trustHtml(
        """
     <div class="alert alert-danger toast-error">
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