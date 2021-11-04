package es.fhir.rest.core;

import ca.uhn.fhir.rest.server.IResourceProvider;

/**
 * Definition of REST resource provider using {@link IFhirTransformer} instances.
 * 
 * @author thomas
 *
 */
public interface IFhirResourceProvider extends IResourceProvider {
	
	/**
	 * Get the {@link IFhirTransformer} used by this {@link IFhirResourceProvider}.
	 * 
	 * @return
	 */
	public IFhirTransformer<?, ?> getTransformer();
}
