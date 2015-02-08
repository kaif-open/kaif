library model_article;

abstract class Voter {
  VoteState get voteState;

  int get previousCount;

  DateTime get updateTime;
}

class ArticleVoter implements Voter {
  final String articleId;
  final VoteState voteState;
  final int previousCount;
  final DateTime updateTime;

  ArticleVoter(this.articleId, this.voteState, this.previousCount, this.updateTime);

  ArticleVoter.decode(Map raw) : this(
      raw['articleId'],
      VoteState.valueOf(raw['voteState']),
      raw['previousCount'],
      new DateTime.fromMillisecondsSinceEpoch(raw['updateTime']));
}


class VoteState {
  static const VoteState UP = const VoteState._('UP', 1);
  static const VoteState DOWN = const VoteState._('DOWN', -1);
  static const VoteState EMPTY = const VoteState._('EMPTY', 0);

  static const List<VoteState> ALL = const [UP, DOWN, EMPTY];

  final String name;
  final int value;

  const VoteState._(this.name, this.value);

  static VoteState valueOf(String name) {
    return ALL.firstWhere((s) => s.name == name);
  }

  dynamic toJson() => name;

  /**
   * calculate total count changed from previous state
   *
   * ex:
   *
   * UP.deltaFrom(DOWN) => +2
   * DOWN.deltaFrom(EMPTY) => -1
   */
  int deltaFrom(VoteState previous) {
    return value - previous.value;
  }
}


class DebateVoter implements Voter {
  final String debateId;
  final VoteState voteState;
  final int previousCount;
  final DateTime updateTime;

  DebateVoter(this.debateId, this.voteState, this.previousCount, this.updateTime);

  DebateVoter.decode(Map raw) : this(
      raw['debateId'],
      VoteState.valueOf(raw['voteState']),
      raw['previousCount'],
      new DateTime.fromMillisecondsSinceEpoch(raw['updateTime']));
}