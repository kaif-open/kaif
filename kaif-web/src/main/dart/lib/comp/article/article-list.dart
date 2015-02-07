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

/**
 * initially voteBox is not click-able (unless user not signed in)
 *
 * it is vote-able after first ajax call completed (check signed in user is voted or not)
 */
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
    voters
    .where((voter) => voter.articleId == articleId)
    .where((voter) => !voter.cancel)
    .forEach((voter) {
      if (previousCount <= voter.previousCount) {
        // web page is cached (counting is stale)
        _changeCount(delta:1);
      }
      _mark(voted:true);
    });

    //allow vote after voters applied
    _refreshClickListener();

    //TODO back off click too fast
    //TODO total vote CD time
  }

  void _refreshClickListener() {
    // clickListener is enabled once, after processing complete, re-enable again
    // so while processing the link is not click-able
    if (accountSession.isSignIn) {
      Function voteListener = isVoted ? _onCancelVote : _onUpVote;
      upVoteAnchorElem.onClick.first
      .then(voteListener)
      .whenComplete(_refreshClickListener);
    } else {
      upVoteAnchorElem.onClick.first
      .then(_onSignUpHint)
      .whenComplete(_refreshClickListener);
    }
  }

  Future _onSignUpHint(Event e) {
    //TODO prompt sign-up-hint, after hint close return future
    print("TODO sign up hint");
    return new Future.value(null);
  }

  Future _onCancelVote(Event e) {
    _mark(voted:false);
    _changeCount(delta:-1);
    return voteService.cancelVoteArticle(zone, articleId).then((_) {
      // does nothing
    }).catchError((e) {
      // revert
      _mark(voted:true);
      _changeCount(delta:1);
      new Toast.error('$e', seconds:5).render();
    });
  }

  Future _onUpVote(Event e) {
    var original = _changeCount(delta:1);
    _mark(voted:true);
    return voteService.upVoteArticle(zone, articleId, original).then((_) {
      // does nothing
    }).catchError((e) {
      //revert
      _mark(voted:false);
      _changeCount(delta:-1);
      new Toast.error('$e', seconds:5).render();
    });
  }

  /**
   * return vote count before change
   */
  int _changeCount({int delta}) {
    var old = previousCount;
    previousCount += delta;
    voteCountElem.text = "${previousCount}";
    return old;
  }

  /**
   * changing voted state and apply visually
   */
  void _mark({bool voted}) {
    elem.classes.toggle(VOTED_CLASS, voted);
  }

  bool get isVoted => elem.classes.contains(VOTED_CLASS);
}