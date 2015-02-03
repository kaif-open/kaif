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
    elem.querySelectorAll('[debate-form]').forEach((el) {
      new DebateForm.placeHolder(el, articleService);
    });
  }

  void _onClickReplier(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();
    Element replier = e.target;
    new DebateReplier(replier, articleService).toggleShow();
  }
}

class DebateReplier {
  final Element elem;
  final ArticleService articleService;
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

  DebateReplier(this.elem, this.articleService) {
    Element placeHolderElem = new DivElement();
    elem.append(placeHolderElem);

    form = new DebateForm.placeHolder(placeHolderElem, articleService)
      ..parentDebateId = elem.dataset['debate-id'];

    elem.onClick.listen((e) {
      toggleShow();
    });
  }
}

