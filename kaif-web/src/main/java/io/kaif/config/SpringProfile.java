package io.kaif.config;

/**
 * common scenario of spring profile, application may subclass to add more profile for
 * customization.
 */
public class SpringProfile {

  public static final String SYSTEM_PROPERTY_KEY = "spring.profiles.active";

  /**
   * activated when deploy to production.
   * <p>
   * in tomcat env, you can add:
   * <p>
   * <pre>
   *
   *     -Dspring.profiles.active=prod \
   *
   * </pre>
   */
  public static final String PROD = "prod";

  /**
   * activated in integration test (for example: JdbcTestCases), typically you activate profile in
   * class by annotation:
   * <p>
   * <pre>
   *
   * @ActiveProfiles( { SpringProfile.TEST })
   * @ContextConfiguration(locations =
   * { "classpath*:spring-rose-context.xml", "classpath*:spring-rose-test-jdbc.xml" })
   * public abstract class JdbcTestCases extends AbstractTransactionalJUnit4SpringContextTests {
   * </pre>
   */
  public static final String TEST = "test";

  /**
   * activated while local development mode
   * <p>
   * for example, before start you FooDebugServer, you may want to active DEV profile:
   * <p>
   * <pre>
   *
   * System.setProperty(&quot;spring.profiles.active&quot;, SpringProfile.DEV);
   *
   * or
   *
   * System.setProperty(SpringProfile.SYSTEM_PROPERTY_KEY, SpringProfile.DEV);
   *
   * </pre>
   */
  public static final String DEV = "dev";

  private SpringProfile() {
  }
}
