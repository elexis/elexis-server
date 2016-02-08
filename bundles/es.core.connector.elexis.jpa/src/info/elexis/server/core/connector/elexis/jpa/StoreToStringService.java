package info.elexis.server.core.connector.elexis.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;

/**
 * Resolve storeToString entities into the resp. objects, and serialize those entities
 * into storeToString strings.
 */
public enum StoreToStringService {

	INSTANCE;

	private static Logger log = LoggerFactory.getLogger(StoreToStringService.class);
	
	public String storeToString(AbstractDBObject adbo) {
		String classKey = ElexisTypeMap.getKeyForObject(adbo);
		if(classKey==null) {
			log.warn("Could not resolve {} to storeToString name", adbo.getClass());
			return null;
		}
		
		return classKey+StringConstants.DOUBLECOLON+adbo.getId();
	}
	

	public AbstractDBObject createFromString(String storeToString) {
		if (storeToString == null) {
			log.warn("StoreToString is null");
			return null;
		}

		String[] split = storeToString.split(StringConstants.DOUBLECOLON);
		if (split.length != 2) {
			log.warn("Array size not 2: " + storeToString);
			return null;
		}

		// map string to classname
		String className = split[0];
		String id = split[1];
		Class<? extends AbstractDBObject> clazz = ElexisTypeMap.get(className);
		if (clazz == null) {
			log.warn("Could not resolve class {}", className);
			return null;
		}

		return ProvidedEntityManager.em().find(clazz, id);
	}

}
