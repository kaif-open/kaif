library dispatch_state_test;

import 'package:unittest/unittest.dart';
import 'package:kaif_web/state/state_machine.dart';

class CountingState extends DispatchState {
  List<dynamic> restTriggers = new List();
  int entered = 0;
  int exited = 0;

  void enter() {
    entered++;
  }

  void exit() {
    exited++;
  }
}

class FooTrigger {
}

class BarTrigger {
}

class StateB extends CountingState {
  List<dynamic> restTriggers = new List();

  State processRest(trigger) {
    restTriggers.add(trigger);
    if (trigger is FooTrigger) {
      return new StateF();
    }
    if (trigger == 'selfTriggerToF') {
      triggerSelf(() => new StateF());
    }
    return StateMachineControl.RETAIN;
  }
}

class StateF extends CountingState {
  List<dynamic> restTriggers = new List();

  State processRest(trigger) {
    restTriggers.add(trigger);
    if (trigger is BarTrigger) {
      return new StateB();
    }
    if (trigger == 'selfTriggerToB') {
      triggerSelf(() => new StateB());
    }
    return StateMachineControl.RETAIN;
  }

  void selfTriggerToDummy() {
    triggerSelf(() => DUMMY_STATE);
  }
}

main() {
  StateB stateB;
  StateMachine machine;
  setUp(() {
    stateB = new StateB();
    machine = new StateMachine(stateB);
  });

  test('dispatch', () {
    machine.initialize();
    StateF stateF = machine.processTrigger(new FooTrigger());
    expect(stateF.runtimeType, StateF);
    StateB newStateB = machine.processTrigger(new BarTrigger());
    expect(newStateB.runtimeType, StateB);
  });

  test('dispatch fall through rest if not dispatch trigger', () {
    machine.initialize();
    machine.processTrigger('any');
    machine.processTrigger('bar');
    expect(stateB.restTriggers, orderedEquals(['any', 'bar']));
  });

  test('trigger self', () {
    machine.initialize();
    StateF newStateF = machine.processTrigger('selfTriggerToF');
    expect(newStateF.runtimeType, StateF);
    expect(stateB.exited, equals(1));
    StateB newStateB = machine.processTrigger('selfTriggerToB');
    expect(newStateB.runtimeType, StateB);
  });

  test('trigger self no effect if self not current state', () {
    machine.initialize();
    StateF newStateF = machine.processTrigger('selfTriggerToF');
    StateB newStateB = machine.processTrigger('selfTriggerToB');
    newStateF.selfTriggerToDummy(); //this take no effect
    State notChanged = machine.processTrigger('for obtain current state');
    expect(newStateB, notChanged);
  });
}