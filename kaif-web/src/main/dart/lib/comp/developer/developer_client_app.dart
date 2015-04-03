library developer_client_app;
import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

class DeveloperClientApp {
  final Element elem;

  DeveloperClientApp(this.elem, ClientAppService clientAppService) {
    new Tabs(elem);
    new CreateClientAppForm(elem.querySelector('[create-client-app-form]'), clientAppService);
    elem.querySelectorAll('[edit-client-app-form]').forEach((el) {
      new EditClientAppForm(el, clientAppService);
    });
  }
}

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
      String clientId = await clientAppService.create(name, description, callbackUri);
      new FlashToast.success(i18n('success'), seconds:2);
      route.reload(hash:"edit-client-app_$clientId");
    } catch (error) {
      new Toast.error("$error").render();
    } finally {
      submit.disabled = false;
    }

  }

}


class EditClientAppForm {
  final Element elem;
  final ClientAppService clientAppService;

  EditClientAppForm(this.elem, this.clientAppService) {
    elem.onSubmit.listen(_onSubmit);
  }

  _onSubmit(Event e) async {
    e
      ..preventDefault()
      ..stopPropagation();

    var clientId = (elem.querySelector('[name=clientIdInput]') as TextInputElement).value;
    var name = (elem.querySelector('[name=nameInput]') as TextInputElement).value;
    var description = (elem.querySelector('[name=descriptionInput]') as TextInputElement).value;
    var callbackUri = (elem.querySelector('[name=callbackUriInput]') as TextInputElement).value;
    var submit = elem.querySelector('[type=submit]');
    submit.disabled = true;
    try {
      await clientAppService.update(clientId, name, description, callbackUri);
      new FlashToast.success(i18n('success'), seconds:2);
      route.reload();
    } catch (error) {
      new Toast.error("$error").render();
    } finally {
      submit.disabled = false;
    }

  }

}