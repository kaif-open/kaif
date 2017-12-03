library zone_form;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

class ZoneForm {
  final Element elem;
  final ZoneService zoneService;
  Alert alert;
  SubmitButtonInputElement submitElem;
  TextInputElement zoneInput;
  TextInputElement aliasNameInput;
  bool canCreateZone = false;

  ZoneForm(this.elem, this.zoneService, AccountSession accountSession) {
    alert = new Alert.append(elem.querySelector('[zone-form-alert]'));
    submitElem = elem.querySelector('[type=submit]');
    elem.onSubmit.listen(_onSubmit);

    zoneInput = elem.querySelector('#zoneInput');

    zoneInput.onKeyUp.map((e) => zoneInput.value.trim()).transform(Throttler.throttle(500)).listen((
        partial) {
      if (!zoneInput.checkValidity()) {
        _showHint(i18n('zone.invalid-zone'), ok:false);
        return;
      }
      zoneService.isZoneAvailable(partial).then((available) {
        _tryToggleSubmit(enable:available);
        String hintText = available ? 'zone.available' : 'zone.zone-already-taken';
        _showHint(i18n(hintText), ok:available);
      });
    });

    aliasNameInput = elem.querySelector('#aliasNameInput');

    if (accountSession.isSignIn) {
      _checkCanCreateZone();
    } else {
      elem.querySelector('[not-sign-in-hint]').classes.toggle('hidden', false);
    }
  }

  void _showHint(String hintText, {bool ok}) {
    var hint = elem.querySelector('.zoneHint');
    hint
      ..classes.toggle('text-success', ok)
      ..classes.toggle('text-danger', !ok)
      ..innerHtml = hintText;
  }

  void _checkCanCreateZone() {
    zoneService.canCreateZone().then((ok) {
      canCreateZone = ok;
      _tryToggleSubmit(enable:true);
      elem.querySelector('[can-not-create-zone-hint]').classes.toggle('hidden', ok);
    }).catchError((e) {
      alert.renderError('$e');
    });
  }

  void _tryToggleSubmit({bool enable}) {
    if (canCreateZone) {
      submitElem.disabled = !enable;
    } else {
      submitElem.disabled = true;
    }
  }

  void _onSubmit(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    alert.hide();
    zoneInput.value = zoneInput.value.trim();
    aliasNameInput.value = aliasNameInput.value.trim();

    _tryToggleSubmit(enable:false);
    var loading = new Loading.small()
      ..renderAfter(submitElem);
    String zone = zoneInput.value;
    zoneService.createZone(zone, aliasNameInput.value).then((_) {
      elem.remove();
      new FlashToast.success(i18n('zone.create-success'), seconds:2);
      route.gotoNewArticlesOfZone(zone);
    }).catchError((e) {
      alert.renderError('${e}');
    }).whenComplete(() {
      _tryToggleSubmit(enable:true);
      loading.remove();
    });
  }

}