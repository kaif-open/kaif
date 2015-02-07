library debate_tree;

import 'dart:html';
import 'package:kaif_web/util.dart';
import 'package:kaif_web/model.dart';
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

class DebateVoteBox {

  static const String VOTED_CLASS = 'vote-box-voted';
  final Element elem;
  final VoteService voteService;
  final AccountSession accountSession;
  final String zone;
  final String articleId;
  int previousCount;
  String debateId;
  Element upVoteAnchorElem;
  Element downVoteAnchorElem;
  Element voteCountElem;
  VoteState currentState;
  List<StreamSubscription> _clickSubscriptions = [];

  DebateVoteBox(this.elem, this.voteService, this.accountSession, this.zone, this.articleId) {
    upVoteAnchorElem = elem.querySelector('[debate-up-vote]');
    downVoteAnchorElem = elem.querySelector('[debate-down-vote]');
    voteCountElem = elem.querySelector('[debate-vote-count]');
    previousCount = int.parse(elem.dataset['debate-vote-count']);
    debateId = elem.dataset['debate-id'];
  }

  void applyVoters(List<DebateVoter> voters) {
    var voter = voters
    .firstWhere((voter) => voter.debateId == debateId, orElse:() => null);

    if (voter == null) {

      _mark(VoteState.EMPTY);

    } else {
      //TODO use debate update time to compensate count, not previousCount
      if (voter.voteState == VoteState.UP) {
        if (previousCount <= voter.previousCount) {
          // web page is cached (counting is stale)
          _changeCount(delta:1);
        }
      } else if (voter.voteState == VoteState.DOWN) {
        if (previousCount >= voter.previousCount) {
          _changeCount(delta:-1);
        }
      } else {
        //empty, does nothing
      }
      _mark(voter.voteState);
    }

    //allow vote after voters applied
    _refreshClickListener();

    //TODO back off click too fast
    //TODO total vote CD time
  }

  void _refreshClickListener() {
    _clickSubscriptions.clear();

    if (accountSession.isSignIn) {

      _clickSubscriptions.add(upVoteAnchorElem.onClick.listen((e) {
        _clickSubscriptions.forEach((sub) => sub.cancel());
        _onClickUpVote(e).whenComplete(_refreshClickListener);
      }));

      _clickSubscriptions.add(downVoteAnchorElem.onClick.listen((e) {
        _clickSubscriptions.forEach((sub) => sub.cancel());
        _onClickDownVote(e).whenComplete(_refreshClickListener);
      }));

    } else {
      [upVoteAnchorElem, downVoteAnchorElem].forEach((anchor) {
        anchor.onClick.first
        .then(_onSignUpHint)
        .whenComplete(_refreshClickListener);
      });
    }
  }

  Future _onSignUpHint(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();
    //TODO prompt sign-up-hint, after hint close return future
    print("TODO sign up hint");
    return new Future.value(null);
  }

  Future _onClickUpVote(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();
    VoteState newState = null;
    int delta = null;
    if (currentState == VoteState.UP) {
      newState = VoteState.EMPTY;
      delta = -1;
    } else if (currentState == VoteState.EMPTY) {
      newState = VoteState.UP;
      delta = 1;
    } else if (currentState == VoteState.DOWN) {
      newState = VoteState.UP;
      delta = 2;
    }
    return _onVote(newState, delta);
  }

  Future _onClickDownVote(Event e) {
    e
      ..preventDefault()
      ..stopPropagation();
    VoteState newState = null;
    int delta = null;
    if (currentState == VoteState.UP) {
      newState = VoteState.DOWN;
      delta = -2;
    } else if (currentState == VoteState.EMPTY) {
      newState = VoteState.DOWN;
      delta = -1;
    } else if (currentState == VoteState.DOWN) {
      newState = VoteState.EMPTY;
      delta = 1;
    }
    return _onVote(newState, delta);
  }

  Future _onVote(VoteState newState, int voteDelta) {
    var originalCount = previousCount;
    var originalState = currentState;
    _changeCount(delta:voteDelta);
    _mark(newState);
    return voteService.voteDebate(
        newState, zone, articleId, debateId, originalState, originalCount)
    .then((_) {
      // does nothing
    }).catchError((e) {
      //revert
      _mark(originalState);
      _changeCount(delta:-voteDelta);
      new Toast.error('$e', seconds:5).render();
    });
  }


  void _changeCount({int delta}) {
    previousCount += delta;
    voteCountElem.text = "${previousCount}";
  }

  /**
   * changing voted state and apply visually
   */
  void _mark(VoteState voteState) {
    currentState = voteState;
    elem.classes.toggle(VOTED_CLASS, voteState != VoteState.EMPTY);
    upVoteAnchorElem.classes.toggle('voted', voteState == VoteState.UP);
    downVoteAnchorElem.classes.toggle('voted', voteState == VoteState.DOWN);
  }

}