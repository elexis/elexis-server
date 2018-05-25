package info.elexis.server.core.common.test;

public class TestSystemPropertyConstants {

	/**
	 * The system is started in basic test mode, this mode enforces:<br>
	 * <ul>
	 * <li>Connection against a self-populated test database</li>
	 * </ul>
	 * Requires boolean parameter.
	 */
	public static final String TEST_MODE = "es.test";

	/**
	 * Applied within the OpenId web application to provide a sample unit-test
	 * client with clientId <code>es-unittest-client</code> and clientSecret
	 * <code>es-unittest-client</code>.<br>
	 * Requires boolean parameter.
	 */
	public static final String TEST_MODE_OPENID = "openid.unit-test";

	public static boolean systemIsInTestMode() {
		String testMode = System.getProperty(TEST_MODE);
		if (testMode != null && !testMode.isEmpty()) {
			if (testMode.equalsIgnoreCase(Boolean.TRUE.toString())) {
				return true;
			}
		}
		return false;
	}

}
