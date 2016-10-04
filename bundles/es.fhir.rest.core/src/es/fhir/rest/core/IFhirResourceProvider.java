package es.fhir.rest.core;

import ca.uhn.fhir.rest.server.IResourceProvider;

public interface IFhirResourceProvider extends IResourceProvider {
	public IFhirTransformer<?, ?> getTransformer();
}
