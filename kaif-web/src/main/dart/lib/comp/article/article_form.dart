library article_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
import 'dart:async';


typedef Future _articleCreator();

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
    titleInput.value = titleInput.value.trim();

    //check Article.TITLE_MIN in java
    var articleTitleMin = 3;
    if (titleInput.value.length < articleTitleMin) {
      alert.renderError(i18n('article.min-title', [articleTitleMin]));
      return;
    }

    TextInputElement urlInput = elem.querySelector('#urlInput');
    TextAreaElement contentInput = elem.querySelector('#contentInput');

    if (urlInput != null) {
      urlInput.value = urlInput.value.trim();

      _runCreate(() {
        return articleService.createExternalLink(zone, urlInput.value, titleInput.value);
      });

    } else if (contentInput != null) {
      contentInput.value = contentInput.value.trim();
      //check Article.CONTENT_MIN in java
      var articleContentMin = 10;
      if (contentInput.value.length < articleContentMin) {
        alert.renderError(i18n('article.min-content', [articleContentMin]));
        return;
      }
      _runCreate(() {
        return articleService.createSpeak(zone, contentInput.value, titleInput.value);
      });
    }
  }

  void _runCreate(_articleCreator articleCreator) {
    submitElem.disabled = true;

    var loading = new Loading.small()
      ..renderAfter(submitElem);
    articleCreator()
    .then((_) {
      elem.remove();
      new Toast.success(i18n('article.create-success'), seconds:2).render().then((_) {
        route.gotoNewArticlesOfZone(zone);
      });
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      submitElem.disabled = false;
      loading.remove();
    });
  }
}