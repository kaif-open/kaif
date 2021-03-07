library debate_tree;

import 'dart:async';
import 'dart:html';

import 'package:kaif_web/model.dart';

import '../article/article-list.dart';
import 'debate_comp.dart';
import 'debate_form.dart';

class DebateTree {
  final Element elem;
  final ArticleService articleService;
  final VoteService voteService;
  final AccountSession accountSession;

  DebateTree(
      this.elem, this.articleService, this.voteService, this.accountSession) {
    var articleElem = elem.querySelector('[article]')!;
    ArticleComp articleComp = new ArticleComp(
        articleElem, voteService, accountSession, articleService);
    var zone = articleComp.zone;

    _initArticleVote(articleComp);

    var articleId = articleComp.articleId;
    elem.querySelectorAll('[debate-form]').forEach((el) {
      new DebateForm.placeHolder(
          el, articleService, accountSession, zone, articleId)
        ..canCloseDebate(false)
        ..show();
    });

    List<DebateComp> debateComps = elem.querySelectorAll('[debate]').map((el) {
      return new DebateComp(el, articleService, voteService, accountSession);
    }).toList();

    _initDebateVoters(debateComps, articleId);
  }

  void _initArticleVote(ArticleComp articleComp) {
    Future<List<ArticleVoter>> future;
    if (accountSession.isSignIn) {
      future = voteService.listArticleVoters([articleComp.articleId]);
    } else {
      future = new Future.value([]);
    }
    future.then((voters) {
      articleComp.voteBox.applyVoters(voters);
    });
  }

  void _initDebateVoters(List<DebateComp> debateComps, String articleId) {
    List<DebateVoteBox> voteBoxes =
        debateComps.map((comp) => comp.voteBox).toList();

    Future<List<DebateVoter>> future;
    if (accountSession.isSignIn) {
      future = voteService.listDebateVoters(articleId);
    } else {
      future = new Future.value([]);
    }

    future.then((voters) {
      voteBoxes.forEach((box) => box.applyVoters(voters));
    });
  }
}
