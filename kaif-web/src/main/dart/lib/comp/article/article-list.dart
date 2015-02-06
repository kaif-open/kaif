library article_list;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
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
      var startArticleId = (elem.querySelector('[name=startArticleIdInput]') as HiddenInputElement).value;
      var endArticleId = (elem.querySelector('[name=endArticleIdInput]') as HiddenInputElement).value;
      future = voteService.listArticleVotersInRange(startArticleId, endArticleId);
    } else {
      future = new Future.value([]);
    }

    future.then((articleVoters) {
      voteBoxes.forEach((box) => box.applyVoters(articleVoters));
    });
  }
}


class ArticleVoteBox {

  static const String VOTED_CLASS = 'vote-box-voted';
  final Element elem;
  final VoteService voteService;
  final AccountSession accountSession;
  final String zone;
  int previousCount;
  String articleId;
  Element upVoteAnchorElem;
  Element voteCountElem;

  ArticleVoteBox(this.elem, this.voteService, this.accountSession, this.zone) {
    upVoteAnchorElem = elem.querySelector('[article-up-vote]');
    voteCountElem = elem.querySelector('[article-vote-count]');
    previousCount = int.parse(elem.dataset['article-vote-count']);
    articleId = elem.dataset['article-id'];
  }

  void applyVoters(List<ArticleVoter> voters) {
    voters.where((voter) => voter.articleId == articleId)
    .forEach((voter) {
      if (previousCount <= voter.previousCount) {
        // web page is cached (counting is stale)
        _plusOne();
      }
      _markVoted();
    });

    //allow vote after voters applied
    if (accountSession.isSignIn) {
      if (isVoted) {
        upVoteAnchorElem.onClick.listen(_onCancelVote);
      } else {
        upVoteAnchorElem.onClick.listen(_onUpVote);
      }
    } else {
      upVoteAnchorElem.onClick.listen(_onSignUpHint);
    }
    //TODO guard click too fast
    //TODO total vote CD time
  }

  void _onSignUpHint(Event e) {
    //TODO prompt sign-up-hint
    print("TODO sign up hint");
  }

  void _onCancelVote(Event e) {
    //TODO cancel if voted
    print("TODO cancel");

    //TODO switch to vote mode after success
  }

  void _onUpVote(Event e) {
    _markVoted();
    _plusOne();
    voteService.upVoteArticle(zone, articleId, previousCount).then((_) {
      //TODO switch to cancel mode
    }).catchError((e) {
      //revert
      voteCountElem.text = "${previousCount}";
      elem.classes.toggle(VOTED_CLASS, false);
      new Toast.error('$e', seconds:5).render();
    });
  }

  void _plusOne() {
    voteCountElem.text = "${previousCount + 1}";
  }

  void _markVoted() {
    elem.classes.toggle(VOTED_CLASS, true);
  }

  bool get isVoted => elem.classes.contains(VOTED_CLASS);
}