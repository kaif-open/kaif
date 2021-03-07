library cookies;

import 'dart:html';

String _decode(s) => Uri.decodeComponent(s.replaceAll(r"\+", ' '));

String _format_date(DateTime datetime) {
  /* To Dart team: why i have to do this?! this is so awkward (need native JS Date()!!§) */
  var day = ['Mon', 'Tue', 'Wed', 'Thi', 'Fri', 'Sat', 'Sun'];
  var mon = [
    'Jan',
    'Feb',
    'Mar',
    'Apr',
    'May',
    'Jun',
    'Jul',
    'Aug',
    'Sep',
    'Oct',
    'Nov',
    'Dec'
  ];

  var _int_to_string = (int i, int pad) {
    var str = i.toString();
    var pads = pad - str.length;
    return (pads > 0) ? '${new List.filled(pads, '0').join('')}$i' : str;
  };

  var utc = datetime.toUtc();
  var hour = _int_to_string(utc.hour, 2);
  var minute = _int_to_string(utc.minute, 2);
  var second = _int_to_string(utc.second, 2);

  return '${day[utc.weekday - 1]}, ${utc.day} ${mon[utc.month - 1]} ${utc.year} ' +
      '${hour}:${minute}:${second} ${utc.timeZoneName}';
}

String? cookieGet(String key) {
  var cookies = document.cookie != null ? document.cookie!.split('; ') : [];

  for (var i = 0, l = cookies.length; i < l; i++) {
    var parts = cookies[i].split('=');
    var name = _decode(parts[0]);

    if (key == name) {
      return parts[1] != null ? _decode(parts[1]) : null;
    }
  }

  return null;
}

void cookieSet(String key, String value,
    {num? expires, String? path, String? domain, bool? secure}) {
  DateTime? expiresDate = null;
  if (expires is num) {
    expiresDate = new DateTime.fromMillisecondsSinceEpoch(
        new DateTime.now().millisecondsSinceEpoch +
            expires.toInt() * 24 * 60 * 60 * 1000);
  }

  var cookie = ([
    Uri.encodeComponent(key),
    '=',
    Uri.encodeComponent(value),
    expiresDate != null ? '; expires=' + _format_date(expiresDate) : '',
    // use expires attribute, max-age is not supported by IE
    path != null ? '; path=' + path : '',
    domain != null ? '; domain=' + domain : '',
    secure != null && secure == true ? '; secure' : ''
  ].join(''));

  document.cookie = cookie;
}

bool cookieRemove(String key, {path, domain, secure}) {
  if (cookieGet(key) != null) {
    // Must not alter options, thus extending a fresh object...
    cookieSet(key, '', expires: -1, path: path, domain: domain, secure: secure);
    return true;
  }
  return false;
}
