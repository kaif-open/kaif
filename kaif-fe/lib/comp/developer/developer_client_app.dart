library developer_client_app;
import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
import 'dart:async';

class DeveloperClientApp {
  final Element elem;

  DeveloperClientApp(this.elem, ClientAppService clientAppService) {
    new Tabs(elem);
    new CreateClientAppForm(elem.querySelector('[create-client-app-form]'), clientAppService);
    elem.querySelectorAll('[edit-client-app-form]').forEach((el) {
      new EditClientAppForm(el, clientAppService);
    });
    elem.querySelectorAll('[debug-client-app-form]').forEach((el) {
      new DebugClientAppForm(el, clientAppService);
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
    var description = (elem.querySelector('[name=descriptionInput]') as TextAreaElement).value;
    var callbackUri = (elem.querySelector('[name=callbackUriInput]') as TextInputElement).value;
    var submit = elem.querySelector('[type=submit]') as ButtonElement;
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
    var description = (elem.querySelector('[name=descriptionInput]') as TextAreaElement).value;
    var callbackUri = (elem.querySelector('[name=callbackUriInput]') as TextInputElement).value;
    var submit = elem.querySelector('[type=submit]') as ButtonElement;
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

class DebugClientAppForm {
  final Element elem;
  final ClientAppService clientAppService;

  DebugClientAppForm(this.elem, this.clientAppService) {
    elem.onSubmit.listen(_onSubmit);
  }

  _onSubmit(Event e) async {
    e
      ..preventDefault()
      ..stopPropagation();

    var clientId = (elem.querySelector('[name=clientIdInput]') as TextInputElement).value;
    var submit = elem.querySelector('[type=submit]') as ButtonElement;
    submit.disabled = true;
    var loading = new Loading.small()
      ..renderAfter(submit);
    try {
      String token = await clientAppService.generateDebugAccessToken(clientId);
      await new Future.delayed(const Duration(seconds:2));
      elem.querySelector('[generated-token-group]').classes.remove('hidden');
      (elem.querySelector('[name=generatedTokenInput]') as TextAreaElement).value = token;
    } catch (error) {
      new Toast.error("$error").render();
    } finally {
      submit.disabled = false;
      loading.remove();
    }
  }
}