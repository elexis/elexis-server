package es.fhir.rest.core.resources;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.UriType;

import info.elexis.server.core.security.oauth2.OAuth2ServiceConstants;

public class ServerCapabilityStatementProvider
		extends org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider {

	@Override
	public CapabilityStatement getServerConformance(HttpServletRequest theRequest) {
		CapabilityStatement serverConformance = super.getServerConformance(theRequest);
		serverConformance.getRest().get(0).setSecurity(getSmartOnFhirCapabilityStatementRestSecurityComponent(theRequest));
		return serverConformance;
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

		String baseUrl = "https://"+theRequest.getLocalName()+":8443";
		
		Extension oauthExtension = new Extension();
		oauthExtension.setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");
		Extension oauthTokenExtension = new Extension();
		oauthTokenExtension.setUrl("token");
		oauthTokenExtension.setValue(new UriType(baseUrl + OAuth2ServiceConstants.TOKEN_ENDPOINT));
		oauthExtension.getExtension().add(oauthTokenExtension);
		
		Extension oauthAuthorizeExtension = new Extension();
		oauthAuthorizeExtension.setUrl("authorize");
		oauthAuthorizeExtension.setValue(new UriType(baseUrl+OAuth2ServiceConstants.AUTHORIZE_ENDPOINT));
		oauthExtension.getExtension().add(oauthAuthorizeExtension);

		csrsc.getService().add(smartOnFhirConcept);
		csrsc.getExtension().add(oauthExtension);

		return csrsc;
	}

}
