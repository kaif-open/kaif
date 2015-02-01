library external_link_article_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

class ExternalLinkArticleForm {

  final Element elem;
  final ArticleService articleService;
  TextInputElement titleInput;
  TextInputElement urlInput;
  Alert alert;

  ExternalLinkArticleForm(this.elem, this.articleService) {
    alert = new Alert.append(elem);
    titleInput = elem.querySelector('#titleInput');
    urlInput = elem.querySelector('#urlInput');
    elem.onSubmit.listen(_onSubmit);
  }

  void _showHint(String hintText, {bool ok}) {
    var hint = elem.querySelector('.nameHint');
    hint
      ..classes.toggle('text-success', ok)
      ..classes.toggle('text-danger', !ok)
      ..innerHtml = hintText;
  }

  void _onSubmit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    alert.hide();

    titleInput.value = titleInput.value.trim();
    urlInput.value = urlInput.value.trim();

    //check Article.TITLE_MIN in java
    if (titleInput.value.length < 3) {
      alert.renderError(i18n('article.min-title', [3]));
      return;
    }

    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    var loading = new Loading.small()
      ..renderAfter(submit);
    articleService.createExternalLink(route.currentZone(), urlInput.value, titleInput.value)
    .then((_) {
      titleInput.value = '';
      urlInput.value = '';
      elem.remove();
      new Toast.success(i18n('article.create-success'), seconds:2).render().then((_) {
        route.gotoCurrentZoneNewArticles();
      });
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      submit.disabled = false;
      loading.remove();
    });
  }

}