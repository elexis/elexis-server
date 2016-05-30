package es.fhir.rest.core;

import java.util.List;

public interface IFhirResourceProviderRegistry {
	public List<IFhirResourceProvider> getResourceProviders();
}
