library debate_list;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'debate_comp.dart';
import 'dart:async';

class DebateList {

  final Element elem;
  final ArticleService articleService;
  final VoteService voteService;
  final AccountSession accountSession;

  DebateList(this.elem, this.articleService, this.voteService, this.accountSession) {

    List<DebateComp> debateComps = elem.querySelectorAll('[debate]').map((el) {
      return new DebateComp(el, articleService, voteService, accountSession);
    }).toList();

    _initDebateVoters(debateComps);
  }

  void _initDebateVoters(List<DebateComp> debateComps) {
    List<DebateVoteBox> voteBoxes = debateComps.map((comp) => comp.voteBox).toList();

    Future<List<DebateVoter>> future;
    if (accountSession.isSignIn) {
      //TODO, change to debateIds
      future = new Future.value([]);
      // voteService.listDebateVoters(articleId);
    } else {
      future = new Future.value([]);
    }

    future.then((voters) {
      voteBoxes.forEach((box) => box.applyVoters(voters));
    });
  }
}
