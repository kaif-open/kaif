part of util;

class Throttler {

  static StreamTransformer throttle<T>(int milliseconds) {
    Duration duration = new Duration(milliseconds:milliseconds);
    Timer lastTimer;
    return new StreamTransformer<T, T>.fromHandlers(handleData: (event, sink) {
      if (lastTimer != null) {
        lastTimer.cancel();
      }
      lastTimer = new Timer(duration, () => sink.add(event));
    });
  }
}