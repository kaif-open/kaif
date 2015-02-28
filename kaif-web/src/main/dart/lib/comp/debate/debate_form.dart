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
  final String zone;
  final String articleId;

  bool _opened = false;
  Element _elem;
  Element _placeHolderElem;
  Element _previewerElem;
  Element _previewCancelElem;
  ButtonElement _previewBtn;
  TextInputElement _contentInput;
  Alert _alert;
  String parentDebateId;
  bool _previewVisible = false;

  canCloseDebate(bool value) {
    _elem.querySelector('[kmark-debate-cancel]').classes.toggle('hidden', !value);
  }

  DebateForm.placeHolder(Element placeHolder,
                         ArticleService _articleService, String zone, String articleId) :
  this._(placeHolder, _articleService, zone, articleId);

  DebateForm._(this._placeHolderElem, this._articleService, this.zone, this.articleId) {
    _elem = _debateFormTemplate.createElement();
    _elem.querySelector('[kmark-preview]').onClick.listen(_onPreview);

    _alert = new Alert.append(_elem);
    _elem.onSubmit.listen(_onSubmit);
    _previewerElem = _elem.querySelector('[kmark-previewer]');
    _previewBtn = _elem.querySelector('[kmark-preview]');
    _elem.querySelector('[kmark-debate-cancel]').onClick.listen(_onCancel);
    _previewCancelElem = _elem.querySelector('[kmark-preview-cancel]');
    _contentInput = _elem.querySelector('textarea[name=contentInput]');
  }

  void _updatePreviewVisibility({bool previewVisible}) {
    _previewVisible = previewVisible;
    _contentInput.classes.toggle('hidden', _previewVisible);
    _previewerElem.classes.toggle('hidden', !_previewVisible);
    _previewBtn.text = _previewVisible ? i18n('kmark.finish-preview')
                       : i18n('kmark.preview');
  }

  void _onCancel(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();
    hide();
  }

  void _onPreview(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    ButtonElement previewBtn = _elem.querySelector('[kmark-preview]');

    if (_previewVisible) {
      _updatePreviewVisibility(previewVisible:false);
      _previewerElem.setInnerHtml('');
      previewBtn.text = i18n('kmark.preview');
      return;
    }

    previewBtn.disabled = true;
    var loading = new Loading.small()
      ..renderAfter(previewBtn);
    _articleService.previewDebateContent(_contentInput.value.trim())
    .then((preview) {
      _updatePreviewVisibility(previewVisible:true);
      unSafeInnerHtml(_previewerElem, preview);
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
    _contentInput.value = _contentInput.value.trim();

    //check Debate.CONTENT_MIN in java
    if (_contentInput.value.length < 10) {
      _alert.renderError(i18n('debate.min-content', [10]));
      return;
    }

    SubmitButtonInputElement submit = _elem.querySelector('[type=submit]');
    submit.disabled = true;

    var loading = new Loading.small()
      ..renderAfter(submit);
    _articleService.debate(zone,
    articleId,
    parentDebateId,
    _contentInput.value)
    .then((_) {
      _contentInput.value = '';
      _elem.remove();
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

  show() {
    if (_opened) {
      return;
    }
    _placeHolderElem.replaceWith(_elem);
    _opened = true;
    _updatePreviewVisibility(previewVisible:false);
  }

  hide() {
    if (!_opened) {
      return;
    }
    _elem.replaceWith(_placeHolderElem);
    _opened = false;
  }
}