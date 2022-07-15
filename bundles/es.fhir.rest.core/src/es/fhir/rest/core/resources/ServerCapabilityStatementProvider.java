package es.fhir.rest.core.resources;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.r4.model.CapabilityStatement;
import org.hl7.fhir.r4.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.UriType;
import org.keycloak.adapters.KeycloakDeployment;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;

public class ServerCapabilityStatementProvider
		extends ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider {

	private KeycloakDeployment keycloakDeployment;

	public ServerCapabilityStatementProvider(RestfulServer theServer, KeycloakDeployment keycloakDeployment){
		super(theServer);
		this.keycloakDeployment = keycloakDeployment;
	}

	@Override
	public CapabilityStatement getServerConformance(HttpServletRequest theRequest,
		RequestDetails requestDetails){
		CapabilityStatement serverConformance =
			(CapabilityStatement) super.getServerConformance(theRequest, requestDetails);
		serverConformance.getRest().get(0)
			.setSecurity(getSmartOnFhirCapabilityStatementRestSecurityComponent(theRequest));
		return serverConformance;
	}
	
	private CapabilityStatementRestSecurityComponent getSmartOnFhirCapabilityStatementRestSecurityComponent(
		HttpServletRequest theRequest){
		CapabilityStatementRestSecurityComponent csrsc =
			new CapabilityStatementRestSecurityComponent();
		
		CodeableConcept smartOnFhirConcept = new CodeableConcept();
		Coding coding = new Coding();
		coding.setSystem("http://hl7.org/fhir/restful-security-service");
		coding.setCode("SMART-on-FHIR");
		smartOnFhirConcept.addCoding(coding);
		smartOnFhirConcept
			.setText("OAuth2 using SMART-on-FHIR profile (see http://docs.smarthealthit.org)");
		
		Extension oauthExtension = new Extension();
		oauthExtension
			.setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");
		
		Extension oauthTokenExtension = new Extension();
		oauthTokenExtension.setUrl("token");
		oauthTokenExtension.setValue(new UriType(keycloakDeployment.getTokenUrl()));
		oauthExtension.getExtension().add(oauthTokenExtension);
		
//		Extension oauthAuthorizeExtension = new Extension();
//		oauthAuthorizeExtension.setUrl("authorize");
//		oauthAuthorizeExtension.setValue(new UriType(keycloakDeployment.getAuthUrl()));
//		oauthExtension.getExtension().add(oauthAuthorizeExtension);
//		
//		oauthRegisterExtension.setUrl("register");
//		oauthRegisterExtension.setValue(new UriType(baseUrl + "/openid/register"));
//		oauthExtension.getExtension().add(oauthRegisterExtension);
//		
//		Extension oauthManageExtension = new Extension();
//		oauthManageExtension.setUrl("manage");
//		oauthManageExtension.setValue(new UriType(baseUrl + "/openid/manage"));
//		oauthExtension.getExtension().add(oauthManageExtension);
		
		csrsc.getService().add(smartOnFhirConcept);
		csrsc.getExtension().add(oauthExtension);
		
		return csrsc;
	}
	
	
	
}
