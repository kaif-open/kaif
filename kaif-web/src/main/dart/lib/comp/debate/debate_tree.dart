library debate_tree;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
import '../vote/votable.dart';
import 'debate_form.dart';
import 'dart:async';

class DebateTree {

  final Element elem;
  final ArticleService articleService;
  final VoteService voteService;
  final AccountSession accountSession;

  DebateTree(this.elem, this.articleService, this.voteService, this.accountSession) {

    var zone = (elem.querySelector('[name=zoneInput]') as HiddenInputElement).value;
    var articleId = (elem.querySelector('[name=articleIdInput]') as HiddenInputElement).value;

    elem.querySelectorAll('[debate-replier]').forEach((Element el) {
      el.onClick.first.then(_onClickReplier);
    });
    elem.querySelectorAll('[debate-form]').forEach((el) {
      new DebateForm.placeHolder(el, articleService);
    });

    List<DebateVoteBox> voteBoxes = elem.querySelectorAll('[debate-vote-box]').map((el) {
      return new DebateVoteBox(el, voteService, accountSession, zone, articleId);
    }).toList();

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

  void _onClickReplier(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();
    Element replier = e.target;
    new DebateReplier(replier, articleService).toggleShow();
  }
}

class DebateReplier {
  final Element elem;
  final ArticleService articleService;
  DebateForm form;
  bool _opened = false;

  toggleShow() {
    if (_opened) {
      form.elem.remove();
    } else {
      elem.parent.append(form.elem);
    }
    _opened = !_opened;
  }

  DebateReplier(this.elem, this.articleService) {
    Element placeHolderElem = new DivElement();
    elem.append(placeHolderElem);

    form = new DebateForm.placeHolder(placeHolderElem, articleService)
      ..parentDebateId = elem.dataset['debate-id'];

    elem.onClick.listen((e) {
      toggleShow();
    });
  }
}

class DebateVoteBox extends Votable {

  final VoteService voteService;
  final AccountSession accountSession;
  final String zone;
  final String articleId;
  String debateId;

  DebateVoteBox(Element elem, this.voteService, this.accountSession, this.zone, this.articleId)
  : super(elem) {
    debateId = elem.dataset['debate-id'];

    var upVoteElem = elem.querySelector('[debate-up-vote]');
    var downVoteElem = elem.querySelector('[debate-down-vote]');
    var voteCountElem = elem.querySelector('[debate-vote-count]');
    var currentCount = int.parse(elem.dataset['debate-vote-count']);
    init(currentCount, upVoteElem, downVoteElem, voteCountElem);
  }

  void applyVoters(List<DebateVoter> voters) {
    if (!accountSession.isSignIn) {
      applyNotSignIn();
      return;
    }

    var voter = voters
    .firstWhere((voter) => voter.debateId == debateId, orElse:() => null);
    if (voter == null) {
      applyNoVoter();
      return;
    }

    applyVoterReady(voter);
  }

  Future onVote(VoteState newState, VoteState previousState, int previousCount) {
    return voteService.voteDebate(
        newState, zone, articleId, debateId, previousState, previousCount);
  }

}