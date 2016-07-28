package es.fhir.rest.core.test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;

public class FhirClient {

	public static IGenericClient getTestClient() {
		FhirContext ctx = FhirContext.forDstu3();
		return ctx.newRestfulGenericClient("http://localhost:8380/fhir");
	}
}
