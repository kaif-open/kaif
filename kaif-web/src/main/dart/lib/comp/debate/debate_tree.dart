library debate_tree;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
import 'debate_form.dart';

class DebateTree {

  final Element elem;
  final ArticleService articleService;

  DebateTree(this.elem, this.articleService) {
    elem.querySelectorAll('[debate-replier]').forEach((Element el) {
      el.onClick.first.then(_onClickReplier);
    });
  }

  void _onClickReplier(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();
    Element replier = e.target;
    var articleId = (elem.querySelector('input[name=articleInput]') as HiddenInputElement).value;
    var zone = (elem.querySelector('input[name=zoneInput]') as HiddenInputElement).value;
    new DebateReplier(replier, articleService, zone, articleId).toggleShow();
  }
}

class DebateReplier {
  final Element elem;
  final ArticleService articleService;
  final String articleId;
  final String zone;
  DebateForm form;
  bool _opened = false;

  toggleShow() {
    if (_opened) {
      form.elem.remove();
    } else {
      elem.parent.append(form.elem);
    }
    _opened = !_opened;
  }

  DebateReplier(this.elem, this.articleService, this.zone, this.articleId) {
    var parentDebateId = elem.dataset['debate-id'];

    //TODO share template from server
    //this is a copy form in debates.ftl
    var formElem = trustHtml("""
        <form class="pure-form" debate-form>
            <input type="hidden" name="zoneInput" value="${zone}">
            <input type="hidden" name="articleInput" value="${articleId}">
            <input type="hidden" name="parentDebateIdInput" value="${parentDebateId}">
            <textarea name="contentInput" maxlength="4096" rows="3"></textarea>
            <button type="submit" class="pure-button pure-button-primary">留言</button>
        </form>
    """);
    form = new DebateForm(formElem, articleService);

    elem.onClick.listen((e) {
      toggleShow();
    });
  }
}

