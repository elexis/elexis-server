package info.elexis.server.core.connector.elexis;

import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.internal.services.locking.LockService;
import info.elexis.server.core.connector.elexis.locking.ILockServiceContributor;

public class Properties {

	/**
	 * A comma separated list of {@link ILockServiceContributor} instances required
	 * to be available to run the {@link LockService}. Used by {@link LockService}
	 */
	public static final String PROPERTY_CONFIG_REQUIRED_LOCK_CONTRIBUTORS = BundleConstants.BUNDLE_ID
			+ ".requiredLockContributors";

	/**
	 * Do accept that required lock contributors are not available. USED FOR TESTING
	 * PURPOSES ONLY!<br>
	 * Can be only activated in combination with {@link #TEST_MODE}.<br>
	 * Requires boolean parameter.
	 */
	public static final String SYSTEM_PROPERTY_ACCEPT_MISSING_LOCKSERVICE_CONTRIBUTORS = "acceptMissingLockServiceContributor";
}
