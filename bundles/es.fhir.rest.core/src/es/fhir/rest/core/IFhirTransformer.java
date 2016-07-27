package es.fhir.rest.core;

import java.util.Optional;

public interface IFhirTransformer<F, L> {
	/**
	 * Create a new FHIR object representing the localObject.
	 * 
	 * @param localObject
	 * @return
	 */
	public Optional<F> getFhirObject(L localObject);

	/**
	 * Search for the local Object matching the FHIR object.
	 * 
	 * @param fhirObject
	 * @return
	 */
	public Optional<L> getLocalObject(F fhirObject);

	/**
	 * Update th local Object matching the FHIR object.
	 * 
	 * @param fhirObject
	 * @return
	 */
	public void updateLocalObject(F fhirObject, L localObject);

	/**
	 * Create a new local Object matching the FHIR object.
	 * 
	 * @param fhirObject
	 * @return
	 */
	public Optional<L> createLocalObject(F fhirObject);

	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz);
}
