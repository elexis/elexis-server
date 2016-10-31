package es.fhir.rest.core.test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

public class FhirClient {

	public static IGenericClient getTestClient() {
		FhirContext ctx = FhirContext.forDstu3();
		// Create a logging interceptor
		LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
		loggingInterceptor.setLogRequestSummary(true);
		loggingInterceptor.setLogRequestBody(true);

		IGenericClient client = ctx.newRestfulGenericClient("http://localhost:8380/fhir");
		client.registerInterceptor(loggingInterceptor);
		return client;
	}
}
