library article_list;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';

class ArticleList {

  final Element elem;
  final ArticleService articleService;
  final VoteService voteService;
  String zone;

  ArticleList(this.elem, this.articleService, this.voteService) {
    zone = (elem.querySelector('[name=zoneInput]') as HiddenInputElement).value;
    elem.querySelectorAll('[article-vote-box]').forEach((Element el) {
      new ArticleVoteBox(el, voteService, zone);
    });
  }
}


class ArticleVoteBox {
  final Element elem;
  final VoteService voteService;
  final String zone;
  int previousCount;
  String articleId;
  Element upVoteAnchor;

  ArticleVoteBox(this.elem, this.voteService, this.zone) {
    upVoteAnchor = elem.querySelector('[article-up-vote]');
    upVoteAnchor.onClick.listen(_onUpVote);
    previousCount = int.parse(elem.dataset['article-vote-count']);
    articleId = elem.dataset['article-id'];
  }

  void _onUpVote(Event e) {
    voteService.upVoteArticle(zone, articleId, previousCount).then((_) {
      elem.classes.toggle('vote-box-voted', true);
      elem.querySelector('[article-vote-count]').text = "${previousCount + 1}";
    });
  }
}