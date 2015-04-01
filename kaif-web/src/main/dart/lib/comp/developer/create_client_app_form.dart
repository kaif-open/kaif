library create_client_app_form.dart;
import 'dart:html';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';

class CreateClientAppForm {
  final Element elem;
  final ClientAppService clientAppService;

  CreateClientAppForm(this.elem, this.clientAppService) {
    elem.onSubmit.listen(_onSubmit);
  }

  _onSubmit(Event e) async {
    e
      ..preventDefault()
      ..stopPropagation();

    var name = (elem.querySelector('[name=nameInput]') as TextInputElement).value;
    var description = (elem.querySelector('[name=descriptionInput]') as TextInputElement).value;
    var callbackUri = (elem.querySelector('[name=callbackUriInput]') as TextInputElement).value;
    var submit = elem.querySelector('[type=submit]');
    submit.disabled = true;
    try {
      await clientAppService.create(name, description, callbackUri);
      new FlashToast.success(i18n('success'), seconds:3);
      route.reload();
    } catch (error) {
      new Toast.error("$error").render();
    } finally {
      submit.disabled = false;
    }

  }

}