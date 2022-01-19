package es.fhir.rest.core.resources;

import org.hl7.fhir.r4.model.BaseResource;

import ca.uhn.fhir.rest.server.IResourceProvider;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;

/**
 * Definition of REST resource provider using {@link IFhirTransformer} instances.
 * 
 * @author thomas
 *
 */
public interface IFhirResourceProvider<T extends BaseResource, U> extends IResourceProvider {
	
	/**
	 * Get the {@link IFhirTransformer} used by this {@link IFhirResourceProvider}.
	 * 
	 * @return
	 */
	public IFhirTransformer<T, U> getTransformer();
}
