package es.fhir.rest.core.servlets;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import es.fhir.rest.core.resources.IFhirResourceProvider;
import es.fhir.rest.core.resources.PlainResourceProvider;

@Component(service = {}, immediate = true)
public class FhirServletResourceCollector {

	private static PlainResourceProvider plainResourceProvider;

	// resource providers
	private static List<IFhirResourceProvider<?, ?>> providers;

	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public synchronized void bindFhirProvider(IFhirResourceProvider<?, ?> provider) {
		if (providers == null) {
			providers = new ArrayList<IFhirResourceProvider<?, ?>>();
		}
		providers.add(provider);
	}

	public void unbindFhirProvider(IFhirResourceProvider<?, ?> provider) {
		if (providers == null) {
			providers = new ArrayList<IFhirResourceProvider<?, ?>>();
		}
		providers.remove(provider);
//		try {
//			unregisterProvider(provider);
//		} catch (Exception e) {
//			logger.warn("Exception unbinding provider [{}]", provider.getClass().getName(), e);
//		}
	}

	@Reference
	public synchronized void bindPlainResourceProvider(PlainResourceProvider plainResourceProvider) {
		FhirServletResourceCollector.plainResourceProvider = plainResourceProvider;
	}

	public static List<IFhirResourceProvider<?, ?>> getProviders() {
		return providers;
	}

	public static PlainResourceProvider getPlainResourceProvider() {
		return plainResourceProvider;
	}

}
