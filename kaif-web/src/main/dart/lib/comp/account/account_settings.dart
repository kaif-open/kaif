library account_settings;

import 'dart:html';
import 'edit_description_form.dart';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class AccountSettings {

  final Element elem;
  final AccountService accountService;
  final AccountSession accountSession;

  AccountSettings(this.elem, this.accountService, this.accountSession) {
    _createReactivateIfRequire();

    new _UpdateNewPasswordForm(
        elem.querySelector('[update-new-password-form]'),
        accountService,
        accountSession);

    new _DescriptionEditor(
        elem.querySelector('[description-content-edit]'),
        elem.querySelector('[description-content]'),
        elem.querySelector('[description-content-editor]'),
        accountService);
  }

  void _createReactivateIfRequire() {
    ButtonElement found = elem.querySelector('#account-reactivate');
    if (found == null) {
      //already activated
      return;
    }

    found.onClick.first.then((e) {
      e
        ..preventDefault()
        ..stopPropagation();
      found.disabled = true;
      accountService.resendActivation().then((_) {
        new Toast.success(i18n('account-settings.reactivation-sent')).render();
      }).catchError((e) {
        new Toast.error(e.toString()).render();
      });
    });
  }
}

class _DescriptionEditor {
  final Element contentElem;
  final Element contentEditElem;
  final Element actionElem;
  final AccountService accountService;
  EditDescriptionForm form;

  _DescriptionEditor(this.contentEditElem, this.contentElem, this.actionElem, this.accountService) {
    actionElem.onClick.listen(_onClick);
  }

  void _onClick(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    accountService.loadEditableDescription()
    .then((content) {
      //lazy create
      if (form == null) {
        form = new EditDescriptionForm.placeHolder(contentEditElem, contentElem, actionElem,
        accountService);
      }
      form
        ..content = content
        ..show();
    }).catchError((e) {
      new Toast.error('$e', seconds:5).render();
    });
  }

}

class _UpdateNewPasswordForm {
  final Element elem;
  final AccountSession accountSession;
  final AccountService accountService;
  Alert alert;

  _UpdateNewPasswordForm(this.elem, this.accountService, this.accountSession) {
    alert = new Alert.append(elem);
    elem.onSubmit.listen(_onSubmit);
  }

  void _onSubmit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();
    TextInputElement oldPasswordInput = elem.querySelector('#oldPasswordInput');
    TextInputElement passwordInput = elem.querySelector('#passwordInput');
    TextInputElement confirmPasswordInput = elem.querySelector('#confirmPasswordInput');
    alert.hide();

    if (passwordInput.value != confirmPasswordInput.value) {
      alert.renderError(i18n('sign-up.password-not-same'));
      return;
    }

    SubmitButtonInputElement submit = elem.querySelector('[type=submit]');
    submit.disabled = true;

    var loading = new Loading.small()
      ..renderAfter(submit);
    accountService.updateNewPassword(oldPasswordInput.value, passwordInput.value)
    .then((AccountAuth auth) {
      accountSession.save(auth);
      elem.remove();
      alert.hide();
      new FlashToast.success(i18n('account-settings.update-new-password-success'), seconds:3);
      route.reload();
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      submit.disabled = false;
      loading.remove();
    });

  }
}