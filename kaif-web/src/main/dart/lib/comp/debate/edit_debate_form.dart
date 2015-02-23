library debate_edit_form;

import 'dart:html';
import 'dart:async';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/comp/kmark/edit_kmark_form.dart';

class EditDebateForm extends EditKmarkForm {

  final ArticleService _articleService;
  String debateId;

  @override
  int get minContentLength => 10;

  @override
  String get contentTooShortMessageKey => 'debate.min-content';

  @override
  String get submitSuccessMessageKey => 'debate.edit-success';

  EditDebateForm.placeHolder (Element contentEditElem, Element contentElement,
                              this._articleService, this.debateId) :
  super.placeHolder(contentEditElem, contentElement);

  @override
  Future<String> preview(String rawInput) {
    return _articleService.previewDebateContent(rawInput);
  }

  @override
  Future<String> submit(String rawInput) {
    return _articleService.updateDebateContent(
        debateId,
        rawInput);
  }
}