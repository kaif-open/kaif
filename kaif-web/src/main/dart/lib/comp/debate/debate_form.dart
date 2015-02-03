library debate_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

class DebateForm {

  final Element elem;
  final ArticleService articleService;
  Alert alert;

  DebateForm(this.elem, this.articleService) {
    alert = new Alert.append(elem);
    elem.onSubmit.listen(_onSubmit);
  }

  void _onSubmit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    //TODO prompt login/registration if not login

    alert.hide();
    TextInputElement contentInput = elem.querySelector('textarea[name=contentInput]');
    HiddenInputElement articleInput = elem.querySelector('input[name=articleInput]');
    HiddenInputElement zoneInput = elem.querySelector('input[name=zoneInput]');
    HiddenInputElement parentDebateIdInput = elem.querySelector('input[name=parentDebateIdInput]');
    contentInput.value = contentInput.value.trim();

    //check Debate.CONTENT_MIN in java
    if (contentInput.value.length < 10) {
      alert.renderError(i18n('debate.min-content', [10]));
      return;
    }

    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    var loading = new Loading.small()
      ..renderAfter(submit);
    articleService.debate(zoneInput.value,
    articleInput.value,
    parentDebateIdInput.value,
    contentInput.value)
    .then((_) {
      contentInput.value = '';
      elem.remove();
      new Toast.success(i18n('debate.create-success'), seconds:2).render().then((_) {
        route.reload();
      });
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      submit.disabled = false;
      loading.remove();
    });
  }

}