part of view;

void scrollToAnchor(LinkElement target, {int duration:300,int offset:100}) {
  String link = target.toString();
  String anchor = link.substring(link.indexOf('#'));
  int targetPosition = querySelector('$anchor').offsetTop;
  targetPosition -= offset;

  int totalFrames = (duration / (1000 / 60)).round();
  int currentFrame = 0;
  int currentPosition = window.scrollY;
  int distanceBetween = targetPosition - currentPosition;
  num distancePerFrame = distanceBetween / totalFrames;
  print(distancePerFrame);

  void animation(num frame) {
    if (totalFrames >= currentFrame) {
      window.scrollTo(0, currentPosition);
      currentPosition += distancePerFrame;

      currentFrame++;
      window.animationFrame.then(animation);
    }
  }
  window.animationFrame.then(animation);
}
