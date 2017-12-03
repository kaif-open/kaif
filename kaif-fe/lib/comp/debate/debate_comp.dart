library debate_comp;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
import '../vote/votable.dart';
import 'debate_form.dart';
import 'edit_debate_form.dart';
import 'dart:async';


class DebateComp {
  final VoteService voteService;
  final AccountSession accountSession;
  final ArticleService articleService;
  final Element elem;
  String zone;
  String articleId;
  String debateId;
  String debaterName;
  bool reloadWhenReply = true;
  DebateVoteBox voteBox;

  DebateComp(this.elem, this.articleService, this.voteService, this.accountSession) {
    debateId = elem.dataset['debate-id'];
    zone = elem.dataset['zone'];
    articleId = elem.dataset['article-id'];
    debaterName = elem.dataset['debater-name'];

    var voteElem = elem.querySelector('[debate-vote-box]');
    var voteCountElem = elem.querySelector('[debate-vote-count]');
    voteBox = new DebateVoteBox(voteElem, this, voteCountElem);

    var replierElem = elem.querySelector('[debate-replier]');
    if (replierElem != null) {
      // null means could not reply (exceed reply limit)
      new DebateReplier(replierElem, this);
    }

    if (accountSession.isSelf(debaterName)) {
      var editorElem = elem.querySelector('[debate-content-editor]');
      Element contentElem = elem.querySelector('[debate-content]');
      Element contentEditElem = elem.querySelector('[debate-content-edit]');
      //if any element missing, this debate is not editable
      if (editorElem == null || contentElem == null || contentEditElem == null) {
        return;
      }
      new DebateEditor(contentElem, contentEditElem, editorElem, this);
    }
  }
}

class DebateEditor {
  final DebateComp debateComp;
  final Element contentElem;
  final Element contentEditElem;
  final Element elem;
  EditDebateForm form;

  DebateEditor(this.contentElem, this.contentEditElem, this.elem, this.debateComp) {
    this.elem.classes.toggle('hidden', false);
    elem.onClick.listen(_onClick);
  }

  void _onClick(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    debateComp.articleService.loadEditableDebate(debateComp.debateId)
    .then((content) {
      //lazy create
      if (form == null) {
        form = new EditDebateForm.placeHolder(contentEditElem, contentElem,
        debateComp.articleService, debateComp.debateId);
      }
      form
        ..content = content
        ..show();
    }).catchError((e) {
      new Toast.error('$e', seconds:5).render();
    });
  }

}

class DebateReplier {
  final Element elem;
  final DebateComp debateComp;

  DebateForm form;

  DebateReplier(this.elem, this.debateComp) {
    elem.onClick.listen(_onClick);
  }

  void _onClick(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();

    //lazy create
    if (form == null) {
      Element placeHolderElem = new DivElement();
      elem.parent.insertAdjacentElement('afterEnd', placeHolderElem);
      form = new DebateForm.placeHolder(
          placeHolderElem, debateComp.articleService, debateComp.accountSession, debateComp.zone,
          debateComp.articleId)
        ..reloadWhenSubmit = debateComp.reloadWhenReply
        ..parentDebateId = debateComp.debateId;
    }
    form.show();
  }
}

class DebateVoteBox extends Votable {

  final DebateComp debateComp;

  DebateVoteBox(Element elem, this.debateComp, Element voteCountElem)
  : super(elem) {
    var upVoteElem = elem.querySelector('[debate-up-vote]');
    var downVoteElem = elem.querySelector('[debate-down-vote]');
    var currentCount = int.parse(elem.dataset['debate-vote-count']);
    init(currentCount, upVoteElem, downVoteElem, voteCountElem);
  }

  void applyVoters(List<DebateVoter> voters) {
    if (!debateComp.accountSession.isSignIn) {
      applyNotSignIn();
      return;
    }

    var voter = voters
    .firstWhere((voter) => voter.debateId == debateComp.debateId, orElse:() => null);
    if (voter == null) {
      applyNoVoter();
      return;
    }

    applyVoterReady(voter);
  }

  Future onVote(VoteState newState, VoteState previousState, int previousCount) {
    return debateComp.voteService.voteDebate(
        newState, debateComp.debateId, previousState,
        previousCount);
  }

}