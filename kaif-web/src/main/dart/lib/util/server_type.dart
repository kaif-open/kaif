part of util;

class ServerType {
  String _locale;
  List<String> _profilesActive;

  /**
   * server detected client locale (may not same as browser locale)
   *
   * only available in dev mode. production return null
   */
  String get locale => _locale;
  List<String> get profilesActive => _profilesActive;
  bool get isDevMode => _profilesActive.contains('dev');

  ServerType() {
    MetaElement localeEl = querySelector('meta[name=kaifLocale]');
    _locale = localeEl == null ? null : localeEl.content;
    MetaElement modeEl = querySelector('meta[name=kaifProfilesActive]');
    _profilesActive = modeEl == null ? 'prod' : modeEl.content.split(',').toList();
  }
}