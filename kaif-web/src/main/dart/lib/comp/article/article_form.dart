library article_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
import 'dart:async';
import '../kmark/kmark_auto_linker.dart';
import 'package:kaif_web/comp/kmark/edit_kmark_form.dart';


typedef Future _articleCreator(String zone);

//TODO auto save draft if speak mode
class ArticleForm {

  final Element elem;
  final ArticleService articleService;
  Alert alert;
  SubmitButtonInputElement submitElem;
  SelectElement zoneInput;
  TextAreaElement contentInput;

  bool get isSpeakMode => contentInput != null;

  bool ignoreDuplicateExternalUrl = false;

  ArticleForm(this.elem, this.articleService, AccountSession accountSession) {
    alert = new Alert.append(elem.querySelector('[alert-section]'));
    submitElem = elem.querySelector('[type=submit]');
    elem.onSubmit.listen(_onSubmit);

    zoneInput = elem.querySelector('[name=zoneInput]');
    zoneInput.onChange.listen((Event e) {
      _checkCanCreateArticleOnZone();
    });

    if (accountSession.isSignIn) {
      zoneInput.disabled = false;
      _checkCanCreateArticleOnZone();
    } else {
      elem.querySelector('[not-sign-in-hint]').classes.toggle('hidden', false);
    }

    contentInput = elem.querySelector('#contentInput');
    if (isSpeakMode) {
      _enableKmark();
    }
  }

  void _enableKmark() {
    new KmarkAutoLinker(contentInput);
    KmarkUtil.enableHelpIfExist(elem);
    var previewerElem = elem.querySelector('[kmark-previewer]');
    ButtonElement previewBtn = elem.querySelector('[kmark-preview]')
      ..classes.remove('hidden');

    previewBtn.onClick.listen((e) async {
      //<ORDER>
      KmarkUtil.alignInputToRenderedHeight(contentInput, previewerElem);
      var previewerHidden = previewerElem.classes.toggle('hidden');
      contentInput.classes.toggle('hidden', !previewerHidden);
      //</ORDER>

      previewBtn.text = previewerHidden ? i18n('kmark.preview')
                        : i18n('kmark.finish-preview');

      if (!previewerHidden) {
        try {
          previewBtn.disabled = true;
          String rendered = await articleService.previewSpeakContent(contentInput.value.trim());
          unSafeInnerHtml(previewerElem, rendered);
        } catch (error) {
          alert.renderError('$error');
        } finally {
          previewBtn.disabled = false;
        }
      }
    });
  }

  void _checkCanCreateArticleOnZone() {
    String zone = zoneInput.value;
    submitElem.disabled = true; //disable first, wait ajax to enable
    if (isStringBlank(zone)) {
      return;
    }
    articleService.canCreateArticle(zone).then((ok) {
      submitElem.disabled = !ok;
      elem.querySelector('[can-not-create-article-hint]').classes.toggle('hidden', ok);
    }).catchError((e) {
      alert.renderError('$e');
    });
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


    if (isSpeakMode) {
      contentInput.value = contentInput.value.trim();
      //check Article.CONTENT_MIN in java
      var articleContentMin = 10;
      if (contentInput.value.length < articleContentMin) {
        alert.renderError(i18n('article.min-content', [articleContentMin]));
        return;
      }
      _runCreate((String zone) {
        return articleService.createSpeak(zone, contentInput.value, titleInput.value);
      });
    } else {
      TextInputElement urlInput = elem.querySelector('#urlInput');
      urlInput.value = urlInput.value.trim();

      _runCreate((String zone) async {
        if (!ignoreDuplicateExternalUrl) {
          if (await articleService.isExternalUrlExist(zone, urlInput.value)) {
            ignoreDuplicateExternalUrl = true;
            submitElem.text = i18n('article.force-create');
            submitElem.classes
              ..remove('pure-button-primary')
              ..add('button-danger');
            throw i18n('article.url-exist');
          }
        }
        return articleService.createExternalLink(zone, urlInput.value, titleInput.value);
      });
    }
  }

  void _runCreate(_articleCreator articleCreator) {
    submitElem.disabled = true;
    String zone = zoneInput.value;
    var loading = new Loading.small()
      ..renderAfter(submitElem);
    articleCreator(zone)
    .then((_) {
      elem.remove();
      new FlashToast.success(i18n('article.create-success'), seconds:2);
      route.gotoNewArticlesOfZone(zone);
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      submitElem.disabled = false;
      loading.remove();
    });
  }
}