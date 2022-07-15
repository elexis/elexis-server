package info.elexis.server.core.servlet.filter;

import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.KeycloakDeploymentBuilder;
import org.keycloak.adapters.spi.HttpFacade.Request;
import org.keycloak.representations.adapters.config.AdapterConfig;

import ch.elexis.core.eenv.IElexisEnvironmentService;

public class ElexisEnvironmentKeycloakConfigResolver implements KeycloakConfigResolver {
	
	private KeycloakDeployment keycloakDeployment;
	private IElexisEnvironmentService elexisEnvironmentService;
	
	/**
	 * 
	 * @param elexisEnvironmentService
	 * @param subId
	 */
	public ElexisEnvironmentKeycloakConfigResolver(
		IElexisEnvironmentService elexisEnvironmentService, String subId){
		
		this.elexisEnvironmentService = elexisEnvironmentService;
		build(subId);
	}
	
	private void build(String subId){
		AdapterConfig adapterConfig = new AdapterConfig();
		
		String clientId = IElexisEnvironmentService.ES_STATION_ID_DEFAULT + "." + subId;
		String secret = System.getenv().get("OAUTH_SECRET_" + clientId.toUpperCase());
		
		adapterConfig.setRealm(IElexisEnvironmentService.EE_KEYCLOAK_REALM_ID);
		String keycloakUrlAuthServerUrl = elexisEnvironmentService.getKeycloakBaseUrl() + "/auth";
		adapterConfig.setAuthServerUrl(keycloakUrlAuthServerUrl);
		adapterConfig.setResource(clientId.toLowerCase());
		// server answers with 401 for unauthenticated users instead of
		// redirecting to login page
		adapterConfig.setBearerOnly(true);
		adapterConfig.setDisableTrustManager(false);
		adapterConfig.getCredentials().put("secret", secret);
		keycloakDeployment = KeycloakDeploymentBuilder.build(adapterConfig);
	}
	
	@Override
	public KeycloakDeployment resolve(Request facade){
		return keycloakDeployment;
	}
	
}
