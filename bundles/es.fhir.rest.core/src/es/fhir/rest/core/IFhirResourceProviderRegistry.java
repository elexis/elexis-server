package es.fhir.rest.core;

import java.util.List;

/**
 * Registry definition used to collect all {@link IFhirResourceProvider}
 * services available.
 * 
 * @author thomas
 *
 */
public interface IFhirResourceProviderRegistry {
	/**
	 * Get all available {@link IFhirResourceProvider};
	 * 
	 * @return
	 */
	public List<IFhirResourceProvider> getResourceProviders();
}
