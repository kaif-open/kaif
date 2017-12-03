library debate_list;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'debate_comp.dart';
import 'dart:async';
import '../server_part_loader.dart';

class DebateList {

  final Element elem;
  final ArticleService articleService;
  final VoteService voteService;
  final AccountSession accountSession;
  final ServerPartLoader serverPartLoader;

  DebateList(this.elem, this.articleService, this.voteService, this.accountSession,
             this.serverPartLoader) {

    List<DebateComp> debateComps = elem.querySelectorAll('[debate]').map((el) {
      return new DebateComp(el, articleService, voteService, accountSession)
        ..reloadWhenReply = false;
    }).toList();

    _initDebateVoters(debateComps);
    new PartLoaderPager(elem, serverPartLoader,
    debateComps.isEmpty ? null : debateComps.last.debateId);
  }

  void _initDebateVoters(List<DebateComp> debateComps) {
    List<DebateVoteBox> voteBoxes = debateComps.map((comp) => comp.voteBox).toList();
    List<String> debateIds = debateComps.map((comp) => comp.debateId).toList();
    Future<List<DebateVoter>> future;
    if (accountSession.isSignIn) {
      future = voteService.listDebateVotersByIds(debateIds);
    } else {
      future = new Future.value([]);
    }

    future.then((voters) {
      voteBoxes.forEach((box) => box.applyVoters(voters));
    });
  }
}
