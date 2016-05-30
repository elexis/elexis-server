package es.fhir.rest.core;

public interface IFhirTransformerRegistry {
	public IFhirTransformer<?, ?> getTransformerFor(Class<?> fhirClazz, Class<?> localClazz);
}
