library article_list;

import 'dart:html';
import 'package:kaif_web/model.dart';
import '../vote/votable.dart';
import 'dart:async';
import '../server_part_loader.dart';
import 'package:kaif_web/util.dart';

class ArticleList {

  final Element elem;
  final ArticleService articleService;
  final VoteService voteService;
  final AccountSession accountSession;

  ArticleList(this.elem, this.articleService, this.voteService, this.accountSession,
              ServerPartLoader serverPartLoader) {
    List<ArticleComp> articleComps = elem.querySelectorAll('[article]').map((Element el) {
      return new ArticleComp(el, voteService, accountSession, articleService);
    }).toList();

    _initArticleVoters(articleComps);

    new PartLoaderPager(elem, serverPartLoader,
    articleComps.isEmpty ? null : articleComps.last.articleId);
  }

  _initArticleVoters(List<ArticleComp> articleComps) {
    Future<List<ArticleVoter>> future;
    if (accountSession.isSignIn) {
      var articleIds = articleComps.map((comp) => comp.articleId).toList();
      future = voteService.listArticleVoters(articleIds);
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
  final ArticleService articleService;

  String _zone;
  String _articleId;
  ArticleVoteBox _voteBox;

  String get zone => _zone;

  String get articleId => _articleId;

  ArticleVoteBox get voteBox => _voteBox;

  ArticleComp(this.elem, this.voteService, this.accountSession, this.articleService) {
    _articleId = elem.dataset['article-id'];
    _zone = elem.dataset['zone'];
    var voteBoxElem = elem.querySelector('[article-vote-box]');
    _voteBox = new ArticleVoteBox(voteBoxElem, this);
    elem.querySelectorAll('[article-deletion]').forEach((targetElem) {
      new ArticleDeletion(targetElem, this);
    });
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
        newState, articleComp.articleId, previousState, previousCount);
  }
}

class ArticleDeletion {
  final ArticleComp articleComp;
  final Element elem;

  ArticleDeletion(this.elem, this.articleComp) {
    String authorName = elem.dataset['author-name'];
    if (!articleComp.accountSession.isSelf(authorName)) {
      return;
    }
    _init(authorName);
  }

  _init(String authorName) async {
    bool canDelete = await articleComp.articleService.canDeleteArticle(authorName,
    articleComp.articleId);
    if (!canDelete) {
      return;
    }
    elem.classes.remove('hidden');
    var deleteElem = elem.querySelector('[delete]');
    elem.querySelector('[confirm-delete]').onClick.listen((e) {
      e.target.classes.add('hidden');
      deleteElem.classes.remove('hidden');
    });
    deleteElem.onClick.listen((e) async {
      try {
        await articleComp.articleService.deleteArticle(articleComp.articleId);
        route.reload();
      } catch (error) {
        new Toast.error('${error}', seconds:2).render();
      }
    });
  }
}