package info.elexis.server.core.connector.elexis.internal;

import java.util.Optional;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnectionUtil;
import info.elexis.server.core.contrib.ApplicationShutdownRegistrar;
import info.elexis.server.core.contrib.IApplicationShutdownListener;

public class Activator implements BundleActivator {

	public static final String BUNDLE_ID = "info.elexis.server.core.connector.elexis";
	
	private static BundleContext context;
	private static IApplicationShutdownListener iasl = new ApplicationShutdownListener();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		Optional<IElexisDataSource> datasource =
				OsgiServiceUtil.getService(IElexisDataSource.class);
		Optional<DBConnection> connection = ElexisDBConnectionUtil.getConnection();
		if(connection.isPresent()) {
			datasource.get().setDBConnection(connection.get());
		}

		ApplicationShutdownRegistrar.addShutdownListener(iasl);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		ApplicationShutdownRegistrar.removeShutdownListener(iasl);

		Activator.context = null;
	}

}
