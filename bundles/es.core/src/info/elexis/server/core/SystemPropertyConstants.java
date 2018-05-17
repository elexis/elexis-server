package info.elexis.server.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemPropertyConstants {

	private static Logger log = LoggerFactory.getLogger(SystemPropertyConstants.class);

	/**
	 * Disable the security filters for all resources. Allows <b>unprotected
	 * access</b> to resources.
	 * <ul>
	 * <li>/fhir</li>
	 * <li>/services</li>
	 * </ul>
	 */
	public static final String DISABLE_WEB_SECURITY = "disable.web.security";

	private static final boolean isDisableWebSecurity = Boolean.valueOf(System.getProperty(DISABLE_WEB_SECURITY))
			? setValueAndLog(true)
			: false;

	public static boolean isDisableWebSecurity() {
		return isDisableWebSecurity;
	}

	private static boolean setValueAndLog(boolean value) {
		log.error(
				"!!!!! Web security is administratively disabled. System is unprotected. Remove [{}] system property !!!!!",
				DISABLE_WEB_SECURITY);
		return true;
	}

}
