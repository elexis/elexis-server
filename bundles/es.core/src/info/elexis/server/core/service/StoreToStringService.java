package info.elexis.server.core.service;

import java.util.Arrays;
import java.util.Optional;

/**
 * Service definition for getting a storeToString representation for an Object,
 * and load an Object from its storeToString representation. <br />
 * <br />
 * The storeToString representation was introduced by the Elexis
 * PersistentObject implementation, as a way to save a reference to an object in
 * the database.<br />
 * <br />
 * Features can contribute their implementation by implementing the
 * {@link StoreToStringContribution} service interface.
 * 
 * @author thomas
 *
 */
public interface StoreToStringService {

	String DOUBLECOLON = "::";

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

	/**
	 * Split a storeToString into an array containing the type and the id
	 * 
	 * @param storeToString
	 * @return a size 2 array with article type [0] and article id [1] or
	 *         <code>null</code> in either [0] or [1]
	 */
	public static String[] splitIntoTypeAndId(String storeToString) {
		String[] split = storeToString.split(DOUBLECOLON);
		return Arrays.copyOf(split, 2);
	}
}
