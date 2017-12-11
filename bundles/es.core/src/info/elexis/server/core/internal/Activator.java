package info.elexis.server.core.internal;

import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.web.jaxrs.ShiroFeature;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

import info.elexis.server.core.security.ElexisServerCompositeRealm;
import info.elexis.server.core.security.oauth2.internal.OAuthService;
import info.elexis.server.core.security.oauth2.internal.TokenEndpoint;

public class Activator implements BundleActivator {

	public static final String PLUGIN_ID = "info.elexis.server.core";

	private static BundleContext context;

	private ServiceRegistration<?> shiroFeatureRegistration;

	private ServiceTracker<HttpService, Object> httpServiceTracker;

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

		SecurityUtils.setSecurityManager(new DefaultSecurityManager(new ElexisServerCompositeRealm()));

		shiroFeatureRegistration = context.registerService(ShiroFeature.class.getName(), new ShiroFeature(), null);

		httpServiceTracker = new ServiceTracker<HttpService, Object>(context, HttpService.class, null) {
			@Override
			public Object addingService(ServiceReference<HttpService> reference) {
				HttpService httpService = (HttpService) this.context.getService(reference);
				try {
					httpService.registerServlet(TokenEndpoint.ENDPOINT, new TokenEndpoint(), null, null);
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				return httpService;
			}

			@Override
			public void removedService(ServiceReference<HttpService> reference, Object service) {
				HttpService httpService = (HttpService) this.context.getService(reference);
				try {
					httpService.unregister(TokenEndpoint.ENDPOINT);
				} catch (IllegalArgumentException exception) {
					exception.printStackTrace();
				}
			}
		};
		httpServiceTracker.open();
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
		httpServiceTracker.close();
	}

	public static URL loadResourceFile(String filename) {
		return context.getBundle().getEntry(filename);
	}

}
