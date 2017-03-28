package info.elexis.server.core.connector.elexis.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
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

		Bundle bundle = FrameworkUtil.getBundle(AbstractDBObject.class);
		if (bundle.getState() != Bundle.ACTIVE) {
			bundle.start();
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
