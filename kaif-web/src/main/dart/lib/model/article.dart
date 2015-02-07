library model_article;

class ArticleVoter {
  final String articleId;
  final bool cancel;
  final int previousCount;
  final DateTime updateTime;

  ArticleVoter(this.articleId, this.cancel, this.previousCount, this.updateTime);

  ArticleVoter.decode(Map raw) : this(
      raw['articleId'],
      raw['cancel'],
      raw['previousCount'],
      new DateTime.fromMillisecondsSinceEpoch(raw['updateTime']));


}


class VoteState {
  static const VoteState UP = const VoteState._('UP');
  static const VoteState DOWN = const VoteState._('DOWN');
  static const VoteState EMPTY = const VoteState._('EMPTY');

  static const List<VoteState> ALL = const [UP, DOWN, EMPTY];

  final String name;

  const VoteState._(this.name);

  static VoteState valueOf(String name) {
    return ALL.firstWhere((s) => s.name == name);
  }
  dynamic toJson() => name;
}


class DebateVoter {
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