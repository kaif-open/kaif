part of util;

class Throttler {

  static StreamTransformer throttle(int milliseconds) {
    Duration duration = new Duration(milliseconds:milliseconds);
    Timer lastTimer;
    return new StreamTransformer.fromHandlers(handleData: (event, sink) {
      if (lastTimer != null) {
        lastTimer.cancel();
      }
      lastTimer = new Timer(duration, () => sink.add(event));
    });
  }
}