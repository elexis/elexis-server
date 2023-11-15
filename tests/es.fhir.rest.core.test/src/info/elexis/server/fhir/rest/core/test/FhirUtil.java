package info.elexis.server.fhir.rest.core.test;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;

public class FhirUtil {

	private static FhirContext context = FhirContext.forR4();

	private static IParser getJsonParser() {
		return context.newJsonParser();
	}

	public static IBaseResource getAsResource(String jsonResource) {
		return getJsonParser().parseResource(jsonResource);
	}

	public static String serializeToString(IBaseResource baseResource) {
		return context.newJsonParser().setPrettyPrint(true).encodeResourceToString(baseResource);
	}

	public static IGenericClient getGenericClient(String theServerBase) {
		// Create a logging interceptor
		LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
		loggingInterceptor.setLogRequestSummary(true);
		loggingInterceptor.setLogRequestBody(true);

		IGenericClient client = context.newRestfulGenericClient(theServerBase);
		client.registerInterceptor(loggingInterceptor);
		return client;
	}

	/**
	 * Get the code String of the first {@link Coding} in the
	 * {@link CodeableConcept} list with matching system.
	 * 
	 * @param system
	 * @param list
	 * @return
	 */
	public static Optional<String> getCodeFromConceptList(String system, List<CodeableConcept> list) {
		if (list != null && !list.isEmpty()) {
			for (CodeableConcept concept : list) {
				Optional<String> found = getCodeFromCodingList(system, concept.getCoding());
				if (found.isPresent()) {
					return found;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the code String of the first {@link Coding} in the list with matching
	 * system.
	 * 
	 * @param system
	 * @param list
	 * @return
	 */
	public static Optional<String> getCodeFromCodingList(String system, List<Coding> list) {
		if (list != null && !list.isEmpty()) {
			for (Coding coding : list) {
				if (coding.getSystem().equals(system) && coding.getCode() != null) {
					return Optional.of(coding.getCode());
				}
			}
		}
		return Optional.empty();
	}
}
