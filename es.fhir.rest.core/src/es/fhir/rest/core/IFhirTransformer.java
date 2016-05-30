package es.fhir.rest.core;

public interface IFhirTransformer<F, L> {
	public F getFhirObject(L localObject);

	public L getLocalObject(F fhirObject);

	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz);
}
