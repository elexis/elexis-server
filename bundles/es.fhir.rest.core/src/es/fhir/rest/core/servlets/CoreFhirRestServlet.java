package es.fhir.rest.core.servlets;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import es.fhir.rest.core.IFhirResourceProvider;

@Component(service = CoreFhirRestServlet.class)
public class CoreFhirRestServlet extends RestfulServer {

	private static Logger logger = LoggerFactory.getLogger(CoreFhirRestServlet.class);

	private static final long serialVersionUID = -4760702567124041329L;

	// HTTP service to register this as servlet
	private HttpService httpService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	public void bindHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	public void unbindHttpService(HttpService httpService) {
		this.httpService = null;
	}

	// resource providers
	private List<IFhirResourceProvider> providers;

	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public synchronized void bindFhirProvider(IFhirResourceProvider provider) {
		if (providers == null) {
			providers = new ArrayList<IFhirResourceProvider>();
		}
		providers.add(provider);
		registerProvider(provider);
	}

	public void unbindFhirProvider(IFhirResourceProvider provider) {
		if (providers == null) {
			providers = new ArrayList<IFhirResourceProvider>();
		}
		providers.remove(provider);
		try {
			unregisterProvider(provider);
		} catch (Exception e) {
			// ignore
		}
	}

	public CoreFhirRestServlet() {
		super(FhirContext.forDstu3());
	}

	@Activate
	public void activate() {
		try {
			httpService.registerServlet("/fhir/*", this, null, null);
		} catch (ServletException | NamespaceException e) {
			logger.error("Could not register FHIR servlet.", e);
		}
	}

	@Deactivate
	public void deactivate() {
		httpService.unregister("/fhir/*");
	}

	/**
	 * The initialize method is automatically called when the servlet is
	 * starting up, so it can be used to configure the servlet to define
	 * resource providers, or set up configuration, interceptors, etc.
	 */
	@Override
	protected void initialize() throws ServletException {
		/*
		 * This server interceptor causes the server to return nicely formatter
		 * and coloured responses instead of plain JSON/XML if the request is
		 * coming from a browser window. It is optional, but can be nice for
		 * testing.
		 */
		registerInterceptor(new ResponseHighlighterInterceptor());

		/*
		 * Tells the server to return pretty-printed responses by default
		 */
		setDefaultPrettyPrint(true);
	}
}
