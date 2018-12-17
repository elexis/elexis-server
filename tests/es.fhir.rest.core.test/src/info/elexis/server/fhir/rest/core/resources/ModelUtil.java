package info.elexis.server.fhir.rest.core.resources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

class ModelUtil {

	private static FhirContext context = FhirContext.forDstu3();
	
	public static IGenericClient getGenericClient(String theServerBase) {
		// Create a logging interceptor
		LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
		loggingInterceptor.setLogRequestSummary(true);
		loggingInterceptor.setLogRequestBody(true);

		IGenericClient client = context.newRestfulGenericClient(theServerBase);
		client.registerInterceptor(loggingInterceptor);
		return client;
	}
}
