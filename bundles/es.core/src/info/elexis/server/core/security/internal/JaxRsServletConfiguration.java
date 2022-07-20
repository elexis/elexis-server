package info.elexis.server.core.security.internal;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.servlet.ServletException;

import org.eclipse.equinox.http.servlet.ExtendedHttpService;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.servlet.KeycloakOIDCFilter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.publisher.ServletConfiguration;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.core.SystemPropertyConstants;
import info.elexis.server.core.servlet.filter.ContextSettingFilter;
import info.elexis.server.core.servlet.filter.ElexisEnvironmentKeycloakConfigResolver;

/**
 * Register the {@link ShiroFilter} with OSGI Jax RS in order to enforce our security requirements
 */
@Component(service = ServletConfiguration.class)
public class JaxRsServletConfiguration implements ServletConfiguration {
	
	private static final String SERVICES_BASE_URL = "/services";
	private static final String OAUTH_CLIENT_POSTFIX = "jaxrs-api";
	private final String SKIP_PATTERN = SERVICES_BASE_URL + "/(elexis|public)/.*";
	
	private Logger log = LoggerFactory.getLogger(JaxRsServletConfiguration.class);
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	protected IModelService coreModelService;
	
	@Reference
	private IContextService contextService;
	
	@Override
	public HttpContext getHttpContext(HttpService httpService, String rootPath){
		Thread.currentThread()
			.setContextClassLoader(JaxRsServletConfiguration.class.getClassLoader());
		ExtendedHttpService extHttpService = (ExtendedHttpService) httpService;
		
		KeycloakConfigResolver keycloakConfigResolver = null;
		
		if (!SystemPropertyConstants.isDisableWebSecurity()) {
			// web security required -  only available via EE / Keycloak setup
			IElexisEnvironmentService elexisEnvironmentService =
				OsgiServiceUtil.getService(IElexisEnvironmentService.class).orElse(null);
			if (elexisEnvironmentService == null) {
				log.error(
					"Web security enabled, but IElexisEnvironmentService is not available. Aborting JAXRS service setup.");
				throw new IllegalStateException();
			}
			
			keycloakConfigResolver = new ElexisEnvironmentKeycloakConfigResolver(
				elexisEnvironmentService, OAUTH_CLIENT_POSTFIX);
		}
		
		try {
			if (keycloakConfigResolver != null) {
				Hashtable<String, String> filterParams = new Hashtable<>();
				// see https://www.keycloak.org/docs/latest/securing_apps/#_servlet_filter_adapter
				filterParams.put(KeycloakOIDCFilter.SKIP_PATTERN_PARAM, SKIP_PATTERN);
				extHttpService.registerFilter(SERVICES_BASE_URL + "/*",
					new KeycloakOIDCFilter(keycloakConfigResolver), filterParams, null);
				
				extHttpService.registerFilter(SERVICES_BASE_URL + "/*",
					new ContextSettingFilter(contextService, coreModelService, SKIP_PATTERN),
					new Hashtable<>(), null);
			} else {
				log.error("--- UNPROTECTED JAXRS API ---");
			}
		} catch (ServletException | NamespaceException e) {
			log.error("Error registering Keycloak filter", e);
		}
		return null;
	}
	
	@Override
	public Dictionary<String, String> getInitParams(HttpService httpService, String rootPath){
		return null;
	}
	
}