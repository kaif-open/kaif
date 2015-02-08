library votable;
import 'package:kaif_web/state/state_machine.dart';
import 'package:kaif_web/model.dart';
import 'package:kaif_web/util.dart';
import 'dart:async';
import 'dart:html';

//TODO back off click too fast
//TODO total vote CD time

/**
 * base class of Vote box, subclass should implement [onVote] method,
 * and call [init] first, then invoke [apply*] methods after voters is ready
 */
abstract class Votable {

  final Element elem;
  final List<StreamSubscription> _clickSubscriptions = [];

  StateMachine _machine;
  int _currentCount;
  Element _upVoteElem;
  Element _downVoteElem;
  Element _voteCountElem;

  Votable(this.elem) {
    _machine = new StateMachine(new _WaitVoterState(this));
  }

  void init(int currentCount,
            Element upVoteElem,
            Element downVoteElem,
            Element voteCountElem) {
    _currentCount = currentCount;
    _upVoteElem = upVoteElem;
    _downVoteElem = downVoteElem;
    _voteCountElem = voteCountElem;
    _machine.initialize();
  }

  void applyNotSignIn() {
    _machine.processTrigger(
        new _VotableTrigger()
          ..noSignIn = true);
  }

  void applyNoVoter() {
    _machine.processTrigger(
        new _VotableTrigger()
          ..noVoter = true);
  }

  void applyVoterReady(Voter voter) {
    _machine.processTrigger(
        new _VotableTrigger()
          ..voterReady = voter);
  }

  void _triggerVotingCompleted(VoteState voteState) {
    _machine.processTrigger(
        new _VotableTrigger()
          ..votingCompleted = voteState);
  }

  void _markVisualState(VoteState voteState) {
    //the implementation should be idempotent because it may invoke multiple times.
    elem.classes.toggle('vote-box-voted', voteState != VoteState.EMPTY);
    _upVoteElem.classes.toggle('voted', voteState == VoteState.UP);
    _downVoteElem.classes.toggle('voted', voteState == VoteState.DOWN);
  }

  void _registerClick() {
    _clickSubscriptions.add(_upVoteElem.onClick.listen((e) {
      e
        ..stopPropagation()
        ..preventDefault();

      _machine.processTrigger(
          new _VotableTrigger()
            ..upVoting = true);
    }));
    _clickSubscriptions.add(_downVoteElem.onClick.listen((e) {
      e
        ..stopPropagation()
        ..preventDefault();

      _machine.processTrigger(
          new _VotableTrigger()
            ..downVoting = true);
    }));
  }

  void _unregisterClick() {
    _clickSubscriptions.forEach((sub) => sub.cancel());
    _clickSubscriptions.clear();
  }

  void _changeCount({int delta}) {
    _currentCount += delta;
    _voteCountElem.text = "${currentCount}";
  }

  get currentCount => _currentCount;

  //sub class to implement
  Future onVote(VoteState newState, VoteState previousState, int previousCount);
}

// all triggers
class _VotableTrigger {
  Voter voterReady;
  bool noVoter;
  bool noSignIn;
  bool upVoting;
  bool downVoting;
  VoteState votingCompleted;
}

// base class of all machine's state
abstract class _VotableState extends State {
  final Votable votable;

  _VotableState(this.votable) {
    // print("[DEBUG] >> transition to ${toString()}");
  }
}


class _WaitVoterState extends _VotableState {

  _WaitVoterState(Votable votable) : super(votable) {
  }

  void enter() {
  }

  void exit() {
  }

  State process(_VotableTrigger trigger) {

    if (trigger.noSignIn == true) {
      return new _WaitSignUpState(votable);
    }

    if (trigger.noVoter == true) {
      return new _EmptyVoteState(votable);
    }

    if (trigger.voterReady != null) {
      Voter voter = trigger.voterReady ;
      if (voter.voteState == VoteState.UP) {
        if (votable.currentCount <= voter.previousCount) {
          // web page is cached (counting is stale)
          votable._changeCount(delta:1);
        }
        return new _UpVotedState(votable);
      } else if (voter.voteState == VoteState.DOWN) {
        if (votable.currentCount >= voter.previousCount) {
          votable._changeCount(delta:-1);
        }
        return new _DownVotedState(votable);
      } else {
        return new _EmptyVoteState(votable);
      }
    }

    return StateMachineControl.RETAIN;
  }

}

class _WaitSignUpState extends _VotableState {
  _WaitSignUpState(Votable votable) : super(votable) {
  }

  void enter() {
    votable._markVisualState(VoteState.EMPTY);
    votable._registerClick();
  }

  void exit() {
    votable._unregisterClick();
  }

  State process(_VotableTrigger trigger) {
    if (trigger.upVoting == true || trigger.downVoting == true) {
      //TODO prompt sign-up-hint, after hint close return future
      print("TODO sign up hint");
      return new _WaitSignUpState(votable);
    }
    return StateMachineControl.RETAIN;
  }
}

class _EmptyVoteState extends _VotableState {
  _EmptyVoteState(Votable votable) : super(votable) {
  }

  void enter() {
    votable._markVisualState(VoteState.EMPTY);
    votable._registerClick();
  }

  void exit() {
    votable._unregisterClick();
  }

  State process(_VotableTrigger trigger) {
    if (trigger.upVoting == true) {
      return new _VotingState(votable,
      VoteState.UP, VoteState.EMPTY, votable.currentCount);
    }
    if (trigger.downVoting == true) {
      return new _VotingState(votable,
      VoteState.DOWN, VoteState.EMPTY, votable.currentCount);
    }
    return StateMachineControl.RETAIN;
  }
}

class _UpVotedState extends _VotableState {
  _UpVotedState(Votable votable) : super(votable) {
  }

  void enter() {
    votable._markVisualState(VoteState.UP);
    votable._registerClick();
  }

  void exit() {
    votable._unregisterClick();
  }

  State process(_VotableTrigger trigger) {
    if (trigger.upVoting == true) {
      return new _VotingState(votable,
      VoteState.EMPTY, VoteState.UP, votable.currentCount);
    }
    if (trigger.downVoting == true) {
      return new _VotingState(votable,
      VoteState.DOWN, VoteState.UP, votable.currentCount);
    }
    return StateMachineControl.RETAIN;
  }
}

class _DownVotedState extends _VotableState {
  _DownVotedState(Votable votable) : super(votable) {
  }

  void enter() {
    votable._markVisualState(VoteState.DOWN);
    votable._registerClick();
  }

  void exit() {
    votable._unregisterClick();
  }

  State process(_VotableTrigger trigger) {
    if (trigger.upVoting == true) {
      return new _VotingState(votable,
      VoteState.UP, VoteState.DOWN, votable.currentCount);
    }
    if (trigger.downVoting == true) {
      return new _VotingState(votable,
      VoteState.EMPTY, VoteState.DOWN, votable.currentCount);
    }
    return StateMachineControl.RETAIN;
  }
}

class _VotingState extends _VotableState {
  final VoteState newState;
  final VoteState previousState;
  final int previousCount;

  _VotingState(Votable votable,
               this.newState, this.previousState, this.previousCount) : super(votable) {

  }

  void enter() {
    //visually visible for user, before actually submit success

    var delta = newState.deltaFrom(previousState);
    votable._changeCount(delta:delta);
    votable._markVisualState(newState);

    votable.onVote(newState, previousState, previousCount)
    .then((_) {
      votable._triggerVotingCompleted(newState);
    }).catchError((e) {
      //revert
      votable._changeCount(delta:-delta);
      new Toast.error('$e', seconds:5).render();
      votable._triggerVotingCompleted(previousState);
    });
  }

  State process(_VotableTrigger trigger) {
    if (trigger.votingCompleted != null) {
      switch (trigger.votingCompleted) {
        case VoteState.UP:
          return new _UpVotedState(votable);
        case VoteState.DOWN:
          return new _DownVotedState(votable);
        case VoteState.EMPTY:
          return new _EmptyVoteState(votable);
      }
    }
    return StateMachineControl.RETAIN;
  }
}
