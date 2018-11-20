package info.elexis.server.openid.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jetty.osgi.boot.OSGiServerConstants;
import org.eclipse.jetty.webapp.WebAppContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IElexisEntityManager;

@Component(service = {})
public class WebappStarter {

	private WebAppContext webapp;

	private ServiceRegistration<?> serviceRegistration;
	
	@Reference
	private IElexisEntityManager elexisEntityManager;

	@Activate
	protected void activate() throws IOException {
		elexisEntityManager.getEntityManager(); // make sure database is initialized
		
		webapp = new WebAppContext();
		webapp.addBean(new JspStarter(webapp.getServletContext().getContextHandler()));
		webapp.setThrowUnavailableOnStartupException(true);

		Dictionary<String, String> props = new Hashtable<>();
		String warFile = FileLocator.toFileURL(Activator.getContext().getBundle().getEntry("lib/openid.war")).getPath();
		props.put("war", warFile);
		props.put("contextPath", "/openid");
		props.put(OSGiServerConstants.MANAGED_JETTY_SERVER_NAME, OSGiServerConstants.MANAGED_JETTY_SERVER_DEFAULT_NAME);
		serviceRegistration = Activator.getContext().registerService(WebAppContext.class.getName(), webapp, props);
	}

	@Deactivate
	protected void deactivate() {
		serviceRegistration.unregister();
	}

}
