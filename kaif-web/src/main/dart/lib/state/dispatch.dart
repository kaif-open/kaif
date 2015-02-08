part of state_machine;

typedef State ToNextState();

class _SelfTrigger {
  final State self;
  final ToNextState toNextState;

  _SelfTrigger(this.self, this.toNextState);
}

abstract class DispatchState extends State implements _StateMachineAware {
  StateMachine _stateMachine;

  void set stateMachine(StateMachine stateMachine) {
    _stateMachine = stateMachine;
  }

  /**
   * subclass should not override this method. to process not match trigger, override [processRest]
   */
  State process(dynamic trigger) {
    if (trigger is _SelfTrigger) {
      if (identical(trigger.self, this)) {
        return trigger.toNextState();
      }
      return StateMachineControl.RETAIN;
    } else {
      return processRest(trigger);
    }
  }

  void triggerSelf(ToNextState toNextState) {
    if (_stateMachine == null) {
      return;
    }
    _stateMachine.processTrigger(new _SelfTrigger(this, toNextState));
  }

  State processRest(dynamic trigger) {
    return StateMachineControl.RETAIN;
  }
}