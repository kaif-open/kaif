library article_list;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
import '../vote/votable.dart';
import 'dart:async';

class ArticleList {

  final Element elem;
  final ArticleService articleService;
  final VoteService voteService;
  final AccountSession accountSession;
  String zone;

  ArticleList(this.elem, this.articleService, this.voteService, this.accountSession) {
    zone = (elem.querySelector('[name=zoneInput]') as HiddenInputElement).value;
    var voteBoxes = elem.querySelectorAll('[article-vote-box]').map((Element el) {
      return new ArticleVoteBox(el, voteService, accountSession, zone);
    }).toList();

    Future<List<ArticleVoter>> future;
    if (accountSession.isSignIn) {
      var oldestArticleId = (elem.querySelector('[name=oldestArticleIdInput]') as HiddenInputElement).value;
      var newestArticleId = (elem.querySelector('[name=newestArticleIdInput]') as HiddenInputElement).value;
      future = voteService.listArticleVotersInRange(oldestArticleId, newestArticleId);
    } else {
      future = new Future.value([]);
    }

    future.then((articleVoters) {
      voteBoxes.forEach((box) => box.applyVoters(articleVoters));
    });
  }
}

class ArticleVoteBox extends Votable {

  final VoteService voteService;
  final AccountSession accountSession;
  final String zone;
  String articleId;

  ArticleVoteBox(Element elem, this.voteService, this.accountSession, this.zone)
  : super(elem) {
    articleId = elem.dataset['article-id'];

    var upVoteElem = elem.querySelector('[article-up-vote]');
    var voteCountElem = elem.querySelector('[article-vote-count]');
    var currentCount = int.parse(elem.dataset['article-vote-count']);

    //not support down vote
    var fakeDownVoteElem = new SpanElement();

    init(currentCount, upVoteElem, fakeDownVoteElem, voteCountElem);
  }

  void applyVoters(List<ArticleVoter> voters) {
    if (!accountSession.isSignIn) {
      applyNotSignIn();
      return;
    }

    var voter = voters
    .firstWhere((voter) => voter.articleId == articleId, orElse:() => null);

    if (voter == null) {
      applyNoVoter();
      return;
    }

    applyVoterReady(voter);
  }

  Future onVote(VoteState newState, VoteState previousState, int previousCount) {
    return voteService.voteArticle(
        newState, zone, articleId, previousState, previousCount);
  }
}