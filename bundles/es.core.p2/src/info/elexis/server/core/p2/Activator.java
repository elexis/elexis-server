package info.elexis.server.core.p2;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import info.elexis.server.core.scheduler.Scheduler;

public class Activator implements BundleActivator {

	public static final String BUNDLE_ID = "info.elexis.server.core.p2";
	
	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		Scheduler.INSTANCE.schedule(Scheduler.DAILY_NIGHT, new SystemUpdateTask());
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
