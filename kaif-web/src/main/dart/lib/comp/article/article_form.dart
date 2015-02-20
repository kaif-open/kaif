library article_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

//TODO auto save draft if speak mode
//TODO kmark preview, for article
class ArticleForm {

  final Element elem;
  final ArticleService articleService;
  String zone;
  Alert alert;
  SubmitButtonInputElement submitElem;

  ArticleForm(this.elem, this.articleService, AccountSession accountSession) {
    alert = new Alert.append(elem);
    submitElem = elem.querySelector('[type=submit]');
    HiddenInputElement zoneInput = elem.querySelector('[name=zoneInput]');
    zone = zoneInput.value;

    if (accountSession.isSignIn) {
      articleService.canCreateArticle(zone).then((ok) {
        if (ok) {
          submitElem.disabled = false;
          elem.onSubmit.listen(_onSubmit);
        } else {
          elem.querySelector('[can-not-create-article-hint]').classes.toggle('hidden', false);
        }
      }).catchError((e) {
        alert.renderError('$e');
      });
    } else {
      elem.querySelector('[not-sign-in-hint]').classes.toggle('hidden', false);
    }
  }

  void _onSubmit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    alert.hide();
    TextInputElement titleInput = elem.querySelector('#titleInput');
    TextInputElement urlInput = elem.querySelector('#urlInput');
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