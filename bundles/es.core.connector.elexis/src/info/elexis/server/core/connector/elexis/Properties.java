package info.elexis.server.core.connector.elexis;

import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.locking.ILockServiceContributor;
import info.elexis.server.core.connector.elexis.services.DocHandleService;
import info.elexis.server.core.connector.elexis.services.LockService;

public class Properties {

	/**
	 * A network path, mounted in the local file-system, used by omnivore to
	 * network-store documents. Used by {@link DocHandleService}
	 */
	public static final String PROPERTY_OMNIVORE_NETWORK_PATH = BundleConstants.BUNDLE_ID + ".omnivoreNetworkPath";

	/**
	 * A comma separated list of {@link ILockServiceContributor} instances
	 * required to be available to run the {@link LockService}. Used by
	 * {@link LockService}
	 */
	public static final String PROPERTY_CONFIG_REQUIRED_LOCK_CONTRIBUTORS = BundleConstants.BUNDLE_ID
			+ ".requiredLockContributors";
}
