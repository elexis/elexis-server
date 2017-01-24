package info.elexis.server.core.service;

import java.util.Optional;

/**
 * Service definition to contribute to the {@link StoreToStringService}. This
 * should be implemented by features that want the {@link StoreToStringService}
 * to be able to store and load their classes.
 * 
 * @author thomas
 *
 */
public interface StoreToStringContribution {

	/**
	 * Find and load an object according to its storeToString
	 * 
	 * @param storeToString
	 * @return loaded Object
	 */
	public Optional<Object> createFromString(String storeToString);

	/**
	 * Generate an Elexis compatible store to string. This requires the mapping
	 * of local entities to their Elexis-respective, PersistentObject-based
	 * counter entities.
	 * 
	 * @param adbo
	 * @return
	 */
	public Optional<String> storeToString(Object object);
}
