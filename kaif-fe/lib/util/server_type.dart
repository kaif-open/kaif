part of util;

class ServerType {
  String? _locale;
  late List<String> _profilesActive;

  /**
   * server detected client locale (may not same as browser locale)
   *
   * only available in dev mode. production return null
   */
  String? get locale => _locale;

  /**
   * server spring active profiles, only meaningful in dev mode. production always
   * return ['prod']
   */
  List<String> get profilesActive => _profilesActive;

  bool get isDevMode => _profilesActive.contains('dev');

  ServerType() {
    MetaElement? localeEl =
        querySelector('meta[name=kaifLocale]') as MetaElement?;
    _locale = localeEl == null ? null : localeEl.content;
    MetaElement? modeEl =
        querySelector('meta[name=kaifProfilesActive]') as MetaElement?;
    _profilesActive =
        modeEl == null ? ['prod'] : modeEl.content.split(',').toList();
  }
}
