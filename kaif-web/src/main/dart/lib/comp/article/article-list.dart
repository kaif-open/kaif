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
    List<ArticleComp> articleComps = elem.querySelectorAll('[article]').map((Element el) {
      return new ArticleComp(el, voteService, accountSession, zone);
    }).toList();

    _initArticleVoters(articleComps);
  }

  _initArticleVoters(List<ArticleComp> articleComps) {
    Future<List<ArticleVoter>> future;
    if (accountSession.isSignIn) {
      var oldestArticleId = (elem.querySelector('[name=oldestArticleIdInput]') as HiddenInputElement).value;
      var newestArticleId = (elem.querySelector('[name=newestArticleIdInput]') as HiddenInputElement).value;
      future = voteService.listArticleVotersInRange(oldestArticleId, newestArticleId);
    } else {
      future = new Future.value([]);
    }

    future.then((articleVoters) {
      articleComps.map((articleComp) => articleComp.voteBox).forEach((
          box) => box.applyVoters(articleVoters));
    });
  }
}

class ArticleComp {
  final Element elem;
  final VoteService voteService;
  final AccountSession accountSession;
  final String zone;
  String articleId;
  ArticleVoteBox voteBox;

  ArticleComp(this.elem, this.voteService, this.accountSession, this.zone) {
    articleId = elem.dataset['article-id'];
    var voteBoxElem = elem.querySelector('[article-vote-box]');
    voteBox = new ArticleVoteBox(voteBoxElem, this);
  }
}

class ArticleVoteBox extends Votable {

  final ArticleComp articleComp;

  ArticleVoteBox(Element elem, this.articleComp)
  : super(elem) {

    var upVoteElem = elem.querySelector('[article-up-vote]');
    var voteCountElem = elem.querySelector('[article-vote-count]');
    var currentCount = int.parse(elem.dataset['article-vote-count']);

    //not support down vote
    var fakeDownVoteElem = new SpanElement();

    init(currentCount, upVoteElem, fakeDownVoteElem, voteCountElem);
  }

  void applyVoters(List<ArticleVoter> voters) {
    if (!articleComp.accountSession.isSignIn) {
      applyNotSignIn();
      return;
    }

    var voter = voters
    .firstWhere((voter) => voter.articleId == articleComp.articleId, orElse:() => null);

    if (voter == null) {
      applyNoVoter();
      return;
    }

    applyVoterReady(voter);
  }

  Future onVote(VoteState newState, VoteState previousState, int previousCount) {
    return articleComp.voteService.voteArticle(
        newState, articleComp.zone, articleComp.articleId, previousState, previousCount);
  }
}