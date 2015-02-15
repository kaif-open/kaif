library debate_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/comp/comp_template.dart';

/**
 * final field in library scope is lazy in dart. so the template only loaded when we
 * actually use [_debateFormTemplate]
 */
final ComponentTemplate _debateFormTemplate = new ComponentTemplate.take('debate-form');

class DebateForm {

  final ArticleService _articleService;

  Element _elem;
  Element _previewer;
  TextInputElement _contentInput;
  Alert _alert;

  Element get elem => _elem;

  String parentDebateId;
  bool _previewVisible = false;

  DebateForm.placeHolder(Element placeHolderElem,
                         ArticleService _articleService) :
  this._(placeHolderElem, _articleService);

  DebateForm._(Element placeHolderElem, this._articleService) {
    _elem = _debateFormTemplate.createElement();
    _elem.querySelector('[kmark-preview]').onClick.listen(_onPreview);
    placeHolderElem.replaceWith(_elem);

    _alert = new Alert.append(_elem);
    _elem.onSubmit.listen(_onSubmit);
    _previewer = elem.querySelector('[kmark-previewer]');
    _contentInput = elem.querySelector('textarea[name=contentInput]');
  }

  void _updatePreviewVisibility(bool previewVisible) {
    _previewVisible = previewVisible;
    _contentInput.classes.toggle('hidden', _previewVisible);
    _previewer.classes.toggle('hidden', !_previewVisible);
  }

  void _onPreview(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    if (_previewVisible) {
      _updatePreviewVisibility(false);
      _previewer.setInnerHtml('');
      return;
    }

    ButtonElement previewBtn = elem.querySelector('[kmark-preview]');
    previewBtn.disabled = true;
    var loading = new Loading.small()
      ..renderAfter(previewBtn);
    _articleService.previewDebateContent(_contentInput.value.trim())
    .then((preview) {
      _updatePreviewVisibility(true);
      unSafeInnerHtml(_previewer, preview);
    }).catchError((e) {
      _alert.renderError('${e}');
    }).whenComplete(() {
      previewBtn.disabled = false;
      loading.remove();
    });
  }

  void _onSubmit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    //TODO prompt login/registration if not login

    _alert.hide();
    HiddenInputElement articleInput = elem.querySelector('input[name=articleInput]');
    HiddenInputElement zoneInput = elem.querySelector('input[name=zoneInput]');
    _contentInput.value = _contentInput.value.trim();

    //check Debate.CONTENT_MIN in java
    if (_contentInput.value.length < 10) {
      _alert.renderError(i18n('debate.min-content', [10]));
      return;
    }

    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    var loading = new Loading.small()
      ..renderAfter(submit);
    _articleService.debate(zoneInput.value,
    articleInput.value,
    parentDebateId,
    _contentInput.value)
    .then((_) {
      _contentInput.value = '';
      elem.remove();
      new Toast.success(i18n('debate.create-success'), seconds:2).render().then((_) {
        route.reload();
      });
    }).catchError((e) {
      _alert.renderError('${e}');
    }).whenComplete(() {
      submit.disabled = false;
      loading.remove();
    });
  }

}