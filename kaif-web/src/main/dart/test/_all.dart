import 'package:unittest/unittest.dart';
import 'state_test.dart' as StateTest;
import 'dispatch_state_test.dart' as DispatchStateTest;

import 'package:unittest/html_enhanced_config.dart';

//import 'package:unittest/interactive_html_config.dart';

main() {

  //useVMConfiguration();

  useHtmlEnhancedConfiguration();

  //useInteractiveHtmlConfiguration();

  group('state machine', () {
    StateTest.main();
    DispatchStateTest.main();
  });
}