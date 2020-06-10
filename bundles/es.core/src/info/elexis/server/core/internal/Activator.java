package info.elexis.server.core.internal;

import java.net.URL;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.web.jaxrs.ShiroFeature;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import info.elexis.server.core.internal.jaxrs.GsonProvider;
import info.elexis.server.core.security.ElexisServerCompositeRealm;

public class Activator implements BundleActivator {
	
	public static final String PLUGIN_ID = "info.elexis.server.core";
	
	private static BundleContext context;
	
	private ServiceRegistration<?> shiroFeatureRegistration;
	private ServiceRegistration<?> registration;
	
	static BundleContext getContext(){
		return context;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception{
		Activator.context = bundleContext;
		
		// JAX-RS gson provider to enforce java.util.Date ISO8601
		// do NOT include the bundle com.eclipsesource.jaxrs.provider.gson
		GsonProvider<?> provider = new GsonProvider<Object>();
		registration = bundleContext.registerService(GsonProvider.class.getName(), provider, null);
		
		// Shiro
		SecurityUtils
			.setSecurityManager(new DefaultSecurityManager(new ElexisServerCompositeRealm()));
		shiroFeatureRegistration =
			context.registerService(ShiroFeature.class.getName(), new ShiroFeature(), null);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception{
		Activator.context = null;
		if (shiroFeatureRegistration != null) {
			shiroFeatureRegistration.unregister();
			shiroFeatureRegistration = null;
		}
		
		if (registration != null) {
			registration.unregister();
		}
	}
	
	public static URL loadResourceFile(String filename){
		return context.getBundle().getEntry(filename);
	}
	
}
