package es.fhir.rest.core.resources;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UriType;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.utils.OsgiServiceUtil;
import jakarta.servlet.http.HttpServletRequest;

public class ServerCapabilityStatementProvider
		extends ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider {


	public ServerCapabilityStatementProvider(RestfulServer theServer) {
		super(theServer);
	}

	@Override
	public CapabilityStatement getServerConformance(HttpServletRequest theRequest,
		RequestDetails requestDetails){
		CapabilityStatement serverConformance =
			(CapabilityStatement) super.getServerConformance(theRequest, requestDetails);
		serverConformance.getRest().get(0)
			.setSecurity(getSmartOnFhirCapabilityStatementRestSecurityComponent(theRequest));
		serverConformance.getRest().get(0).getExtension().add(getWebsocketCapabilityExtension());
		return serverConformance;
	}
	
	private Extension getWebsocketCapabilityExtension() {
		Extension websocketCapability = new Extension();
		websocketCapability.setUrl("http://hl7.org/fhir/StructureDefinition/capabilitystatement-websocket");
		websocketCapability.setValue(new UriType("/fhir/websocketR4")); // forwarded via nginx in EE setup
		return websocketCapability;
	}

	private CapabilityStatementRestSecurityComponent getSmartOnFhirCapabilityStatementRestSecurityComponent(
			HttpServletRequest theRequest) {
		CapabilityStatementRestSecurityComponent csrsc = new CapabilityStatementRestSecurityComponent();

		CodeableConcept smartOnFhirConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setSystem("http://hl7.org/fhir/restful-security-service");
		coding.setCode("SMART-on-FHIR");
		smartOnFhirConcept.addCoding(coding);
		smartOnFhirConcept.setText("OAuth2 using SMART-on-FHIR profile (see http://docs.smarthealthit.org)");

		Extension oauthExtension = new Extension();
		oauthExtension.setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");

		String eeKeycloakRealmEndpoint = StringUtils.EMPTY;
		IElexisEnvironmentService eeService = OsgiServiceUtil.getService(IElexisEnvironmentService.class).orElse(null);
		if (eeService != null) {
			eeKeycloakRealmEndpoint = eeService.getKeycloakRealmEndpoint();
			OsgiServiceUtil.ungetService(eeService);
		}

		Extension oauthTokenExtension = new Extension();
		oauthTokenExtension.setUrl("token");
		if (StringUtils.isNotBlank(eeKeycloakRealmEndpoint)) {
			oauthTokenExtension.setValue(new UriType(eeKeycloakRealmEndpoint + "protocol/openid-connect/token"));
		} else {
			oauthTokenExtension
					.setValue(new UriType("/keycloak/auth/realms/ElexisEnvironment/protocol/openid-connect/token"));
		}
		oauthExtension.getExtension().add(oauthTokenExtension);

		Extension authTokenExtension = new Extension();
		authTokenExtension.setUrl("authorize");
		if (StringUtils.isNotBlank(eeKeycloakRealmEndpoint)) {
			authTokenExtension.setValue(new UriType(eeKeycloakRealmEndpoint + "protocol/openid-connect/auth"));
		} else {
			authTokenExtension
					.setValue(new UriType("/keycloak/auth/realms/ElexisEnvironment/protocol/openid-connect/auth"));
		}
		oauthExtension.getExtension().add(authTokenExtension);

		csrsc.getService().add(smartOnFhirConcept);
		csrsc.getExtension().add(oauthExtension);

		return csrsc;
	}
	
	
	
}
