library debate_edit_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/comp/comp_template.dart';

/**
 * final field in library scope is lazy in dart. so the template only loaded when we
 * actually use [_debateFormTemplate]
 */
final ComponentTemplate _editDebateFormTemplate = new ComponentTemplate.take('edit-debate-form');

class DebateEditForm {

  final ArticleService _articleService;
  TextInputElement _contentInput;
  Element _elem;
  Element _contentElement;
  Element _previewer;
  Element _contentEditElem;
  Alert _alert;
  String debateId;
  bool _opened = false;
  bool _previewVisible = false;

  Element get elem => _elem;

  set content(String content) {
    _contentInput.setInnerHtml(content);
  }

  DebateEditForm.placeHolder (Element contentEditElem, Element contentElement,
                              ArticleService _articleService) :
  this._(contentEditElem, contentElement, _articleService);

  DebateEditForm._ (this._contentEditElem, this._contentElement, this._articleService) {
    _elem = _editDebateFormTemplate.createElement();
    _elem.querySelector('[kmark-preview]').onClick.listen(_onPreview);
    _contentInput = _elem.querySelector('textarea[name=contentInput]');
    _previewer = elem.querySelector('[kmark-previewer]');
    elem.querySelector('[kmark-debate-cancel]').onClick.listen(_onCancel);
    _alert = new Alert.append(_elem);
    _elem.onSubmit.listen(_onSubmit);
  }

  void _onCancel(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();
    toggleShow(false);
  }

  void _onPreview(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    ButtonElement previewBtn = elem.querySelector('[kmark-preview]');

    if (_previewVisible) {
      _updatePreviewVisibility(false);
      _previewer.setInnerHtml('');
      previewBtn.text = i18n('debate.preview');
      return;
    }

    previewBtn.disabled = true;
    var loading = new Loading.small()
      ..renderAfter(previewBtn);
    _articleService.previewDebateContent(_contentInput.value.trim())
    .then((preview) {
      _updatePreviewVisibility(true);
      unSafeInnerHtml(_previewer, preview);
      _previewer.style.minHeight = '1em';
    }).catchError((e) {
      _alert.renderError('${e}');
    }).whenComplete(() {
      previewBtn
        ..disabled = false
        ..text = i18n('debate.finish-preview');
      loading.remove();
    });
  }

  void _onSubmit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    _alert.hide();
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
    _articleService.updateDebateContent(
        debateId,
        _contentInput.value)
    .then((content) {
      toggleShow(false);
      unSafeInnerHtml(_contentElement, content);
      _contentInput.setInnerHtml('');
      new Toast.success(i18n('debate.edits-success'), seconds:2).render();
    }).catchError((e) {
      _alert.renderError('${e}');
    }).whenComplete(() {
      submit.disabled = false;
      loading.remove();
    });
  }

  void toggleShow(bool open) {
    if (open == _opened) {
      return;
    }
    if (_opened) {
      _elem.remove();
    } else {
      //<ORDER>
      _contentEditElem.append(_elem);
      CssStyleDeclaration cssStyleDeclaration = _contentInput.getComputedStyle();

      _contentInput
        ..style.width = (
          _contentElement.clientWidth).toString() + 'px'
        ..style.minHeight = '8em'
        ..style.height = (
          new Dimension.css(cssStyleDeclaration.paddingTop).value
          + new Dimension.css(cssStyleDeclaration.paddingBottom).value
          + _contentElement.clientHeight).toString() + 'px';
      //</ORDER>
    }
    _contentEditElem.classes.toggle('hidden', _opened);
    _contentElement.classes.toggle('hidden', !_opened);
    _opened = !_opened;
  }


  void _updatePreviewVisibility(bool previewVisible) {
    _previewVisible = previewVisible;
    _contentInput.classes.toggle('hidden', _previewVisible);
    _previewer.classes.toggle('hidden', !_previewVisible);
  }
}