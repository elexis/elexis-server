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
	 * Update the local Object with the content of the FHIR object.
	 * 
	 * @param fhirObject
	 * @param localObject
	 * @return the updated local Object, possibly not same as localObject
	 *         parameter, empty if nothing changed
	 */
	public Optional<L> updateLocalObject(F fhirObject, L localObject);

	/**
	 * Create a new local Object matching the FHIR object.
	 * 
	 * @param fhirObject
	 * @return
	 */
	public Optional<L> createLocalObject(F fhirObject);

	/**
	 * Test if the implementation has matching FHIR and local class types.
	 * 
	 * @param fhirClazz
	 * @param localClazz
	 * @return
	 */
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz);
}
