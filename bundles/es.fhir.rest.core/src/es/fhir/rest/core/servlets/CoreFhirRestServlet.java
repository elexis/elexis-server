package es.fhir.rest.core.servlets;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.ServletException;

import org.eclipse.equinox.http.servlet.ExtendedHttpService;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
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
import org.slf4j.bridge.SLF4JBridgeHandler;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.OsgiServiceUtil;
import es.fhir.rest.core.resources.IFhirResourceProvider;
import es.fhir.rest.core.resources.ServerCapabilityStatementProvider;
import info.elexis.server.core.SystemPropertyConstants;
import info.elexis.server.core.servlet.filter.ContextSettingFilter;
import info.elexis.server.core.servlet.filter.ElexisEnvironmentKeycloakConfigResolver;

@Component(service = CoreFhirRestServlet.class, immediate = true)
public class CoreFhirRestServlet extends RestfulServer {
	
	private static final String FHIR_BASE_URL = "/fhir";
	private static final String OAUTH_CLIENT_POSTFIX = "fhir-api";
	private final String SKIP_PATTERN = FHIR_BASE_URL + "/metadata";
	
	private static Logger logger = LoggerFactory.getLogger(CoreFhirRestServlet.class);
	
	private static final long serialVersionUID = -4760702567124041329L;
	
	@Reference
	private HttpService httpService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	protected IModelService coreModelService;
	
	@Reference
	private IContextService contextService;
	
	// resource providers
	private List<IFhirResourceProvider<?, ?>> providers;
	
	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public synchronized void bindFhirProvider(IFhirResourceProvider<?, ?> provider){
		if (providers == null) {
			providers = new ArrayList<IFhirResourceProvider<?, ?>>();
		}
		providers.add(provider);
		registerProvider(provider);
	}
	
	public void unbindFhirProvider(IFhirResourceProvider<?, ?> provider){
		if (providers == null) {
			providers = new ArrayList<IFhirResourceProvider<?, ?>>();
		}
		providers.remove(provider);
		try {
			unregisterProvider(provider);
		} catch (Exception e) {
			logger.warn("Exception unbinding provider [{}]", provider.getClass().getName(), e);
		}
	}
	
	public CoreFhirRestServlet(){
		super(FhirContext.forR4());
		setServerName("Elexis-Server FHIR");
		setServerVersion("3.10");
	}
	
	@Activate
	public void activate(){
		
		// TODO extract for general usage
		//		https://stackoverflow.com/questions/9117030/jul-to-slf4j-bridge
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("").setLevel(Level.INFO);
		
		KeycloakConfigResolver keycloakConfigResolver = null;
		
		if (!SystemPropertyConstants.isDisableWebSecurity()) {
			// web security required -  only available via EE / Keycloak setup
			IElexisEnvironmentService elexisEnvironmentService =
				OsgiServiceUtil.getService(IElexisEnvironmentService.class).orElse(null);
			if (elexisEnvironmentService == null) {
				logger.error(
					"Web security enabled, but IElexisEnvironmentService is not available. Aborting FHIR service setup.");
				throw new IllegalStateException();
			}
			
			keycloakConfigResolver = new ElexisEnvironmentKeycloakConfigResolver(
				elexisEnvironmentService, OAUTH_CLIENT_POSTFIX);
			
			setServerConformanceProvider(
				new ServerCapabilityStatementProvider(this, keycloakConfigResolver.resolve(null)));
		}
		
		Thread.currentThread().setContextClassLoader(CoreFhirRestServlet.class.getClassLoader());
		ExtendedHttpService extHttpService = (ExtendedHttpService) httpService;
		try {
			if (keycloakConfigResolver != null) {
				Hashtable<String, String> filterParams = new Hashtable<>();
				// see https://www.keycloak.org/docs/latest/securing_apps/#_servlet_filter_adapter
				filterParams.put(KeycloakOIDCFilter.SKIP_PATTERN_PARAM, SKIP_PATTERN);
				// TODO role fhir-api-access required? https://www.baeldung.com/spring-boot-keycloak
				extHttpService.registerFilter(FHIR_BASE_URL + "/*",
					new KeycloakOIDCFilter(keycloakConfigResolver), filterParams, null);
				
				extHttpService.registerFilter(FHIR_BASE_URL + "/*",
					new ContextSettingFilter(contextService, coreModelService, SKIP_PATTERN),
					new Hashtable<>(), null);
			} else {
				logger.error("--- UNPROTECTED FHIR API ---");
			}
			
			httpService.registerServlet(FHIR_BASE_URL + "/*", this, null, null);
			
		} catch (ServletException | NamespaceException e) {
			logger.error("Could not register FHIR servlet.", e);
		}
	}
	
	@Deactivate
	public void deactivate(){
		logger.debug("Deactivating CoreFhirRestServlet");
		httpService.unregister(FHIR_BASE_URL + "/*");
	}
	
	/**
	 * The initialize method is automatically called when the servlet is starting up, so it can be
	 * used to configure the servlet to define resource providers, or set up configuration,
	 * interceptors, etc.
	 */
	@Override
	protected void initialize() throws ServletException{
		/*
		 *  This interceptor is used to generate a new log line (via SLF4j) for each incoming request.
		 */
		LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
		registerInterceptor(loggingInterceptor);
		loggingInterceptor.setMessageFormat(
			"REQ ${requestHeader.user-agent}@${remoteAddr} ${operationType} ${idOrResourceName} [${requestParameters}] [${requestBodyFhir}]");
		loggingInterceptor.setErrorMessageFormat(
			"REQ_ERR ${requestHeader.user-agent}@${remoteAddr} ${operationType} ${idOrResourceName} [${requestParameters}] - ${exceptionMessage} [${requestBodyFhir}]");
		
		/*
		 * This server interceptor causes the server to return nicely formatter and
		 * coloured responses instead of plain JSON/XML if the request is coming from a
		 * browser window. It is optional, but can be nice for testing.
		 */
		registerInterceptor(new ResponseHighlighterInterceptor());
		
		/*
		 * Tells the server to return pretty-printed responses by default
		 */
		setDefaultPrettyPrint(true);
	}
}
