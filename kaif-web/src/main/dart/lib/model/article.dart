library model_article;

class ArticleVoter {
  final String voterId;
  final String articleId;
  final bool cancel;
  final int previousCount;
  final DateTime updateTime;

  ArticleVoter(this.voterId, this.articleId, this.cancel, this.previousCount, this.updateTime);

  ArticleVoter.decode(Map raw) : this(
      raw['voterId'],
      raw['articleId'],
      raw['cancel'],
      raw['previousCount'],
      new DateTime.fromMillisecondsSinceEpoch(raw['updateTime'] * 1000));
}