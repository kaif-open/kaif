library edit_description_form;

import 'dart:html';
import 'dart:async';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/comp/kmark/edit_kmark_form.dart';

class EditDescriptionForm extends EditKmarkForm {

  final AccountService _accountService;
  final Element _actionElem;

  EditDescriptionForm.placeHolder(Element contentEditElem,
                                  Element contentElement,
                                  this._actionElem,
                                  this._accountService) : super.placeHolder(
      contentEditElem,
      contentElement);

  @override
  Future<String> preview(String rawInput) {
    return _accountService.previewDescription(rawInput);
  }

  @override
  Future<String> submit(String rawInput) {
    return _accountService.updateDescription(rawInput);
  }

  @override
  String get submitSuccessMessageKey => 'account-setting.update-description-success';

  @override
  String get contentTooShortMessageKey => 'account-setting.min-description';

  @override
  int get minContentLength => 0;

  @override
  void hide() {
    super.hide();
    _actionElem.classes.toggle('hidden', false);
  }

  @override
  void show() {
    super.show();
    _actionElem.classes.toggle('hidden', true);
  }
}