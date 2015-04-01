library edit_kamrk_form;

import 'dart:html';
import 'dart:async';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/comp/comp_template.dart';
import 'kmark_auto_linker.dart';

final ComponentTemplate _editKmarkFormTemplate = new ComponentTemplate.take('edit-kmark-form');

abstract class EditKmarkForm {

  TextAreaElement _contentInput;
  Element _elem;
  Element _contentElem;
  Element _previewerElem;
  Element _contentEditElem;
  ButtonElement _previewBtn;
  Alert _alert;
  bool _opened = false;
  bool _previewVisible = false;

  set content(String content) {
    _contentInput.setInnerHtml(content);
  }

  EditKmarkForm.placeHolder (Element contentEditElem, Element contentElement) :
  this._(contentEditElem, contentElement);

  EditKmarkForm._ (this._contentEditElem, this._contentElem) {
    _elem = _editKmarkFormTemplate.createElement();
    _elem.querySelector('[kmark-preview]').onClick.listen(_onPreview);
    _previewBtn = _elem.querySelector('[kmark-preview]');
    _contentInput = _elem.querySelector('textarea[name=contentInput]');
    _previewerElem = _elem.querySelector('[kmark-previewer]');
    _elem.querySelector('[kmark-cancel]').onClick.listen(_onCancel);
    _alert = new Alert.append(_elem);
    _elem.onSubmit.listen(_onSubmit);
    new KmarkAutoLinker(_contentInput);
    KmarkHelper.enableIfExist(_elem);
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


    if (_previewVisible) {
      _updatePreviewVisibility(previewVisible:false);
      _previewerElem.setInnerHtml('');
      return;
    }

    _previewBtn.disabled = true;
    var loading = new Loading.small()
      ..renderAfter(_previewBtn);
    preview(_contentInput.value.trim())
    .then((preview) {
      _updatePreviewVisibility(previewVisible:true);
      unSafeInnerHtml(_previewerElem, preview);
    }).catchError((e) {
      _alert.renderError('${e}');
    }).whenComplete(() {
      _previewBtn .disabled = false;
      loading.remove();
    });
  }

  Future<String> preview(String rawInput);

  Future<String> submit(String rawInput);

  int get minContentLength;

  String get submitSuccessMessageKey;

  String get contentTooShortMessageKey;

  void _onSubmit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    _alert.hide();
    _contentInput.value = _contentInput.value.trim();

    if (_contentInput.value.length < minContentLength) {
      _alert.renderError(i18n(contentTooShortMessageKey, [minContentLength]));
      return;
    }

    SubmitButtonInputElement submitBtn = _elem.querySelector('[type=submit]');
    submitBtn.disabled = true;

    var loading = new Loading.small()
      ..renderAfter(submitBtn);
    submit(_contentInput.value)
    .then((content) {
      hide();
      unSafeInnerHtml(_contentElem, content);
      _contentInput.setInnerHtml('');
      _previewerElem.setInnerHtml('');
      new Toast.success(i18n(submitSuccessMessageKey), seconds:2).render();
    }).catchError((e) {
      _alert.renderError('${e}');
    }).whenComplete(() {
      submitBtn.disabled = false;
      loading.remove();
    });
  }

  void show() {
    if (_opened) {
      return;
    }
    //<ORDER>
    _contentEditElem.append(_elem);
    CssStyleDeclaration cssStyleDeclaration = _contentInput.getComputedStyle();

    _contentInput
      ..style.height = (
        new Dimension.css(cssStyleDeclaration.paddingTop).value
        + new Dimension.css(cssStyleDeclaration.paddingBottom).value
        + _contentElem.clientHeight).toString() + 'px';
    //</ORDER>
    _contentEditElem.classes.toggle('hidden', false);
    _contentElem.classes.toggle('hidden', true);
    _opened = true;
    _updatePreviewVisibility(previewVisible:false);
  }

  void hide() {
    if (!_opened) {
      return;
    }
    _elem.remove();
    _contentEditElem.classes.toggle('hidden', true);
    _contentElem.classes.toggle('hidden', false);
    _opened = false;
  }

  void _updatePreviewVisibility({bool previewVisible}) {
    _previewVisible = previewVisible;
    _contentInput.classes.toggle('hidden', _previewVisible);
    _previewerElem.classes.toggle('hidden', !_previewVisible);
    _previewBtn.text = _previewVisible ? i18n('kmark.finish-preview')
                       : i18n('kmark.preview');
  }
}

class KmarkHelper {
  static void enableIfExist(Element parentElem) {
    Element toggleElem = parentElem.querySelector('[kmark-help-toggle]');
    Element helpElem = parentElem.querySelector('[kmark-help]');
    if (toggleElem == null || helpElem == null) {
      return;
    }
    toggleElem.onClick.listen((e) {
      e
        ..stopPropagation()
        ..preventDefault();
      bool isHidden = helpElem.classes.toggle('hidden');
      toggleElem.text = isHidden ? i18n("kmark.help") : i18n("kmark.finish-help");
    });
  }
}