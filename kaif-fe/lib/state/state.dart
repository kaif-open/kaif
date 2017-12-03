part of state_machine;

abstract class State {
  const State();

  State process(dynamic trigger) {
    return StateMachineControl.RETAIN;
  }

  void enter() {
  }

  void exit() {
  }
}

abstract class _StateMachineAware {
  void set stateMachine(StateMachine stateMachine);
}

const State DUMMY_STATE = const _DummyState();

class _DummyState extends State {
  const _DummyState() : super();
}

class _RetainState extends State {
  const _RetainState() : super();
}

class _TerminatedState extends State {
  const _TerminatedState() : super();
}

class _TerminateState extends State {
  const _TerminateState() : super();
}

class StateMachineControl {
  static const State RETAIN = const _RetainState();
  static const State TERMINATE = const _TerminateState();
}

class StateMachine {
  static const State _TERMINATED = const _TerminatedState();
  State _current;

  StateMachine(State initialState) {
    _current = initialState;
  }

  void initialize() {
    _setupStateMachineAware();
    _current.enter();
  }

  void _setupStateMachineAware() {
    if (_current is _StateMachineAware) {
      (_current as _StateMachineAware).stateMachine = this;
    }
  }

  void _tearDownStateMachineAware() {
    if (_current is _StateMachineAware) {
      (_current as _StateMachineAware).stateMachine = null;
    }
  }

  void terminate() {
    _current.exit();
    _tearDownStateMachineAware();
    _current = _TERMINATED;
  }

  /**
   * return new current [State]
   */
  State processTrigger(dynamic trigger) {
    State next = _current.process(trigger);
    if (next == StateMachineControl.RETAIN) {
      return _current;
    }
    if (next == StateMachineControl.TERMINATE) {
      terminate();
      return _current;
    }
    _current.exit();
    _tearDownStateMachineAware();
    _current = next;
    _setupStateMachineAware();
    _current.enter();
    return _current;
  }

  bool get isTerminated => _current == _TERMINATED;
}