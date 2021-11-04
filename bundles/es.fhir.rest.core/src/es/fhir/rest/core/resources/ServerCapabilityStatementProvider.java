package es.fhir.rest.core.resources;

import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.UriType;

import ca.uhn.fhir.rest.api.server.RequestDetails;

public class ServerCapabilityStatementProvider
		extends org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider {
	
	@Override
	public CapabilityStatement getServerConformance(HttpServletRequest theRequest,
		RequestDetails requestDetails){
		CapabilityStatement serverConformance =
			super.getServerConformance(theRequest, requestDetails);
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
		
		String baseUrl = getBaseUrl(theRequest);
		
		Extension oauthExtension = new Extension();
		oauthExtension
			.setUrl("http://fhir-registry.smarthealthit.org/StructureDefinition/oauth-uris");
		
		Extension oauthTokenExtension = new Extension();
		oauthTokenExtension.setUrl("token");
		oauthTokenExtension.setValue(new UriType(baseUrl + "/openid/token"));
		oauthExtension.getExtension().add(oauthTokenExtension);
		
		Extension oauthAuthorizeExtension = new Extension();
		oauthAuthorizeExtension.setUrl("authorize");
		oauthAuthorizeExtension.setValue(new UriType(baseUrl + "/openid/authorize"));
		oauthExtension.getExtension().add(oauthAuthorizeExtension);
		
		Extension oauthRegisterExtension = new Extension();
		oauthRegisterExtension.setUrl("register");
		oauthRegisterExtension.setValue(new UriType(baseUrl + "/openid/register"));
		oauthExtension.getExtension().add(oauthRegisterExtension);
		
		Extension oauthManageExtension = new Extension();
		oauthManageExtension.setUrl("manage");
		oauthManageExtension.setValue(new UriType(baseUrl + "/openid/manage"));
		oauthExtension.getExtension().add(oauthManageExtension);
		
		csrsc.getService().add(smartOnFhirConcept);
		csrsc.getExtension().add(oauthExtension);
		
		return csrsc;
	}
	
	private String getBaseUrl(HttpServletRequest theRequest){
		// TODO consider String XForwardedProto = theRequest.getHeader("X-Forwarded-Proto");
		return theRequest.getScheme() + "://" + theRequest.getServerName() + ":"
			+ theRequest.getServerPort();
	}
	
}
