library state_test;
import 'package:unittest/unittest.dart';
import 'package:kaif_web/state/state_machine.dart';

class CountingState extends State {
  int entered = 0;
  int exited = 0;

  void enter() {
    entered++;
  }

  void exit() {
    exited++;
  }
}

class StateA extends CountingState {
  State process(dynamic trigger) {
    if (trigger == 'gotoStateB') {
      return new StateB();
    }
    return StateMachineControl.RETAIN;
  }
}

class StateB extends CountingState {
  State process(dynamic trigger) {
    if (trigger == 'gotoStateA') {
      return new StateA();
    }
    if (trigger == 'die') {
      return StateMachineControl.TERMINATE;
    }
    return StateMachineControl.RETAIN;
  }
}

main() {
  StateA stateA;
  StateMachine machine;
  setUp(() {
    stateA = new StateA();
    machine = new StateMachine(stateA);
  });

  test('initialize', () {
    machine.initialize();
    expect(1, stateA.entered);
    expect(machine.isTerminated, isFalse);
  });

  test('terminate', () {
    machine.initialize();
    machine.terminate();
    expect(1, stateA.entered);
    expect(1, stateA.exited);
    expect(machine.isTerminated, isTrue);
  });

  test('terminated should not change end state', () {
    machine.initialize();
    machine.terminate();

    State end = machine.processTrigger('any');
    expect(end, machine.processTrigger('foo'));
    expect(end, machine.processTrigger('bar'));

  });

  test('trigger to next state', () {
    machine.initialize();
    StateB stateB = machine.processTrigger('gotoStateB');
    expect(1, stateA.entered);
    expect(1, stateA.exited);
    expect(1, stateB.entered);
  });

  test('trigger to more states', () {
    machine.initialize();
    StateB stateB = machine.processTrigger('gotoStateB');
    StateA newStateA = machine.processTrigger('gotoStateA');
    expect(1, stateA.entered);
    expect(1, stateA.exited);
    expect(1, stateB.entered);
    expect(1, stateB.exited);
    expect(1, newStateA.entered);
    expect(0, newStateA.exited);

    machine.processTrigger('gotoStateB');
    machine.processTrigger('die');
    expect(machine.isTerminated, isTrue);
  });
}