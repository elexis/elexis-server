package info.elexis.server.core.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.scheduler.Scheduler;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "info.elexis.server.core";

	private static BundleContext context;

	private static Logger log = LoggerFactory.getLogger(Activator.class);

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		Scheduler.INSTANCE.startScheduler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Scheduler.INSTANCE.stopScheduler();

		Activator.context = null;
	}
	
	public static URL loadResourceFile(String filename) {
		return context.getBundle().getEntry(filename);
	}

}
