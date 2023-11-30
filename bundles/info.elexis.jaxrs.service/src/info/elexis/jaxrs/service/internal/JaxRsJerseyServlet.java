package info.elexis.jaxrs.service.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.logging.Level;

import javax.servlet.ServletException;
import javax.ws.rs.core.Application;

import org.eclipse.equinox.http.servlet.ExtendedHttpService;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import info.elexis.jaxrs.service.JaxrsResource;

@Component(service = JaxRsJerseyServlet.class, immediate = true)
public class JaxRsJerseyServlet extends ServletContainer {

	private static final long serialVersionUID = -131084922708281927L;

	public static final String ALIAS = "/services";

	@Reference
	private HttpService httpService;

	private Set<JaxrsResource> jaxrsServletSet = Collections.synchronizedSet(new HashSet<>());

	@Activate
	public void activate() {

		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("").setLevel(Level.INFO);

		ExtendedHttpService extHttpService = (ExtendedHttpService) httpService;
		try {
			extHttpService.registerServlet(ALIAS, this, new Hashtable<>(), null);
		} catch (ServletException | NamespaceException e) {
			LoggerFactory.getLogger(getClass()).error("activate()", e);
		}

		ResourceConfig resourceConfig = ResourceConfig.forApplicationClass(Application.class);
		jaxrsServletSet.forEach(resourceConfig::register);
		reload(resourceConfig);
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public synchronized void bind(JaxrsResource jaxRsServlet) {
		jaxrsServletSet.add(jaxRsServlet);
	}

	public synchronized void unbind(JaxrsResource jaxRsServlet) {
		jaxrsServletSet.remove(jaxRsServlet);
	}

	@Deactivate
	public void deactivate() {
		ExtendedHttpService extHttpService = (ExtendedHttpService) httpService;
		extHttpService.unregister(ALIAS);
	}

}
