package info.elexis.server.core.connector.elexis.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import info.elexis.server.core.contrib.ApplicationShutdownRegistrar;
import info.elexis.server.core.contrib.IApplicationShutdownListener;

public class Activator implements BundleActivator {

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
