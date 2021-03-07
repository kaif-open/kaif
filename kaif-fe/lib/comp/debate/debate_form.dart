library debate_form;

import 'dart:html';

import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

import '../comp_template.dart';
import '../kmark/edit_kmark_form.dart';
import '../kmark/kmark_auto_linker.dart';

/**
 * final field in library scope is lazy in dart. so the template only loaded when we
 * actually use [_debateFormTemplate]
 */
final ComponentTemplate _debateFormTemplate =
    new ComponentTemplate.take('debate-form');

class DebateForm {
  final ArticleService _articleService;
  final AccountSession _accountSession;
  final String zone;
  final String articleId;

  bool _opened = false;
  late Element _elem;
  Element _placeHolderElem;
  late Element _previewerElem;
  late ButtonElement _previewBtn;
  late TextAreaElement _contentInput;
  late Alert _alert;
  String? parentDebateId;
  bool reloadWhenSubmit = true;
  bool _previewVisible = false;

  canCloseDebate(bool value) {
    _elem
        .querySelector('[kmark-debate-cancel]')!
        .classes
        .toggle('hidden', !value);
  }

  DebateForm.placeHolder(Element placeHolder, ArticleService _articleService,
      AccountSession _accountSession, String zone, String articleId)
      : this._(placeHolder, _articleService, _accountSession, zone, articleId);

  DebateForm._(this._placeHolderElem, this._articleService,
      this._accountSession, this.zone, this.articleId) {
    _elem = _debateFormTemplate.createElement();
    _elem.querySelector('[kmark-preview]')!.onClick.listen(_onPreview);

    _alert = new Alert.append(_elem);
    _elem.onSubmit.listen(_onSubmit);
    _previewerElem = _elem.querySelector('[kmark-previewer]')!;
    _previewBtn = _elem.querySelector('[kmark-preview]') as ButtonElement;
    _elem.querySelector('[kmark-debate-cancel]')!.onClick.listen(_onCancel);
    _contentInput =
        _elem.querySelector('textarea[name=contentInput]') as TextAreaElement;
    new KmarkAutoLinker(_contentInput);
    KmarkUtil.enableHelpIfExist(_elem);
  }

  void _updatePreviewVisibility({required bool previewVisible}) {
    _previewVisible = previewVisible;
    _contentInput.classes.toggle('hidden', _previewVisible);
    _previewerElem.classes.toggle('hidden', !_previewVisible);
    _previewBtn.text =
        _previewVisible ? i18n('kmark.finish-preview') : i18n('kmark.preview');
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

    ButtonElement previewBtn =
        _elem.querySelector('[kmark-preview]') as ButtonElement;

    if (_previewVisible) {
      _updatePreviewVisibility(previewVisible: false);
      _previewerElem.setInnerHtml('');
      previewBtn.text = i18n('kmark.preview');
      return;
    }

    previewBtn.disabled = true;
    var loading = new Loading.small()..renderAfter(previewBtn);
    _articleService
        .previewDebateContent(_contentInput.value?.trim() ?? "")
        .then((preview) {
      _updatePreviewVisibility(previewVisible: true);
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

    if (!_accountSession.isSignIn) {
      var actionElem = new AnchorElement()
        ..href = route.signIn
        ..text = i18n('account-menu.sign-in');
      new SnackBar(i18n('debate.sign-in-to-debate'), action: actionElem)
          .render();
      return;
    }

    _alert.hide();
    _contentInput.value = _contentInput.value?.trim() ?? "";

    //check Debate.CONTENT_MIN in java
    if (_contentInput.value!.length < 5) {
      _alert.renderError(i18n('debate.min-content', [5]));
      return;
    }

    ButtonElement submit =
        _elem.querySelector('[type=submit]') as ButtonElement;
    submit.disabled = true;

    var loading = new Loading.small()..renderAfter(submit);
    _articleService
        .debate(articleId, parentDebateId, _contentInput.value!)
        .then((String debateId) {
      _contentInput.value = '';
      if (reloadWhenSubmit) {
        new FlashToast.success(i18n('debate.create-success'), seconds: 2);
        route.reload();
      } else {
        _elem.remove();
        new Toast.success(i18n('debate.create-success'), seconds: 2).render();
        // note that if no reload, the debate form component is corrupted and could
        // not submit again (currently this is fine)
      }
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
    _updatePreviewVisibility(previewVisible: false);
  }

  hide() {
    if (!_opened) {
      return;
    }
    _elem.replaceWith(_placeHolderElem);
    _opened = false;
  }
}
