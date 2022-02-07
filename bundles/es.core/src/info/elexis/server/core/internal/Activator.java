package info.elexis.server.core.internal;

import java.net.URL;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import info.elexis.server.core.internal.jaxrs.GsonProvider;

public class Activator implements BundleActivator {
	
	public static final String PLUGIN_ID = "info.elexis.server.core";
	
	private static BundleContext context;
	
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
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception{
		Activator.context = null;
		
		if (registration != null) {
			registration.unregister();
		}
	}
	
	public static URL loadResourceFile(String filename){
		return context.getBundle().getEntry(filename);
	}
	
}
