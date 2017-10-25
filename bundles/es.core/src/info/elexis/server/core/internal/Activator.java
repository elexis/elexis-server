package info.elexis.server.core.internal;

import java.net.URL;

import org.apache.shiro.web.jaxrs.ShiroFeature;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "info.elexis.server.core";

	private static BundleContext context;

	private ServiceRegistration<?> shiroFeatureRegistration;

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

		shiroFeatureRegistration = context.registerService(ShiroFeature.class.getName(), new ShiroFeature(), null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
		if (shiroFeatureRegistration != null) {
			shiroFeatureRegistration.unregister();
			shiroFeatureRegistration = null;
		}
	}

	public static URL loadResourceFile(String filename) {
		return context.getBundle().getEntry(filename);
	}

}
