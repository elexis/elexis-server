package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirResourceProviderRegistry;

@Component
public class FhirResourceProviderRegistry implements IFhirResourceProviderRegistry {

	private List<IFhirResourceProvider> providers;

	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC)
	public synchronized void bindFhirProvider(IFhirResourceProvider provider) {
		if (providers == null) {
			providers = new ArrayList<IFhirResourceProvider>();
		}
		providers.add(provider);
	}

	public void unbindFhirProvider(IFhirResourceProvider provider) {
		if (providers == null) {
			providers = new ArrayList<IFhirResourceProvider>();
		}
		providers.remove(provider);
	}

	@Override
	public List<IFhirResourceProvider> getResourceProviders() {
		// make a copy, just in case ...
		List<IFhirResourceProvider> resourceProviders = new ArrayList<IFhirResourceProvider>();
		resourceProviders.addAll(providers);
		return resourceProviders;
	}
}
