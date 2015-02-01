library external_link_article_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

class ExternalLinkArticleForm {

  final Element elem;
  final ArticleService articleService;
  Alert alert;

  ExternalLinkArticleForm(this.elem, this.articleService) {
    alert = new Alert.append(elem);

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
    TextInputElement titleInput = elem.querySelector('#titleInput');
    TextInputElement urlInput = elem.querySelector('#urlInput');
    HiddenInputElement zoneInput = elem.querySelector('#zoneInput');
    titleInput.value = titleInput.value.trim();
    urlInput.value = urlInput.value.trim();

    //check Article.TITLE_MIN in java
    if (titleInput.value.length < 3) {
      alert.renderError(i18n('article.min-title', [3]));
      return;
    }

    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    String zone = zoneInput.value;
    var loading = new Loading.small()
      ..renderAfter(submit);
    articleService.createExternalLink(zone, urlInput.value, titleInput.value)
    .then((_) {
      titleInput.value = '';
      urlInput.value = '';
      elem.remove();
      new Toast.success(i18n('article.create-success'), seconds:2).render().then((_) {
        route.gotoNewArticlesOfZone(zone);
      });
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      submit.disabled = false;
      loading.remove();
    });
  }

}