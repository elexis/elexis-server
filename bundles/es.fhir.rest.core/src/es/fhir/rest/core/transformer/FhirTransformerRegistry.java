package es.fhir.rest.core.transformer;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;

@Component
public class FhirTransformerRegistry implements IFhirTransformerRegistry {

	private List<IFhirTransformer<?, ?>> transformers;

	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY)
	public synchronized void bindFhirTransformer(IFhirTransformer<?, ?> transformer) {
		if (transformers == null) {
			transformers = new ArrayList<IFhirTransformer<?, ?>>();
		}
		transformers.add(transformer);
	}

	public void unbindFhirTransformer(IFhirTransformer<?, ?> transformer) {
		if (transformers == null) {
			transformers = new ArrayList<IFhirTransformer<?, ?>>();
		}
		transformers.remove(transformer);
	}

	public IFhirTransformer<?, ?> getTransformerFor(Class<?> fhirClazz, Class<?> localClazz) {
		for (IFhirTransformer<?, ?> iFhirTransformer : transformers) {
			if (iFhirTransformer.matchesTypes(fhirClazz, localClazz)) {
				return iFhirTransformer;
			}
		}
		return null;
	}
}
