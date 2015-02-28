library debate_list;

import 'dart:html';
import 'package:kaif_web/model.dart';
import 'debate_comp.dart';
import 'dart:async';
import '../server_part_loader.dart';
import 'package:kaif_web/util.dart';

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
    _initPager(debateComps.isEmpty ? null : debateComps.last.debateId);
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

  void _initPager(String startDebateId) {
    if (startDebateId == null) {
      return;
    }
    Element pagerAnchor = elem.querySelector('[debate-list-pager]');
    if (pagerAnchor == null) {
      return;
    }

    pagerAnchor.onClick.first.then((e) {
      e
        ..preventDefault()
        ..stopPropagation();
      pagerAnchor.remove();
      //note this searching globally because we need it to be outside of component
      Element nextWrapper = elem.querySelector('[next-debate-list]');
      //move next list to outside of current debate-list
      elementInsertAfter(elem, nextWrapper);

      //load next page, this will create another DebateList
      serverPartLoader.loadInto(nextWrapper,
      route.currentPartTemplatePath() + "?startDebateId=${startDebateId}",
      loading:new Loading.largeCenter());
    });
  }
}
