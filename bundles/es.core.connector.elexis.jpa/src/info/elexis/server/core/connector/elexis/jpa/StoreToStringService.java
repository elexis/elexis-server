package info.elexis.server.core.connector.elexis.jpa;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;

/**
 * Resolve storeToString entities into the resp. objects, and serialize those entities
 * into storeToString strings.
 */
public enum StoreToStringService {

	INSTANCE;

	private static final BidiMap<String, Class<? extends AbstractDBObject>> stsClassBidiMap;
	private static Logger log = LoggerFactory.getLogger(StoreToStringService.class);
	
	static {
		stsClassBidiMap = new DualHashBidiMap<String, Class<? extends AbstractDBObject>>();
		stsClassBidiMap.put("ch.artikelstamm.elexis.common.ArtikelstammItem", ArtikelstammItem.class);
		stsClassBidiMap.put("ch.elexis.data.TarmedLeistung", TarmedLeistung.class);
		// TODO add other values
	}
	
	public String storeToString(AbstractDBObject adbo) {
		String classKey = stsClassBidiMap.getKey(adbo.getClass());
		if(classKey==null) {
			log.warn("Could not resolve {} to storeToString name", adbo.getClass());
			return null;
		}
		
		return classKey+StringConstants.DOUBLECOLON+adbo.getId();
	}
	

	public AbstractDBObject createFromString(String dbData) {
		if (dbData == null) {
			log.warn("StoreToString is null");
			return null;
		}

		String[] split = dbData.split(StringConstants.DOUBLECOLON);
		if (split.length != 2) {
			log.warn("Array size not 2: " + dbData);
			return null;
		}

		// map string to classname
		String className = split[0];
		String id = split[1];
		Class<? extends AbstractDBObject> clazz = stsClassBidiMap.get(className);
		if (clazz == null) {
			log.warn("Could not resolve class {}", className);
			return null;
		}

		return ProvidedEntityManager.em().find(clazz, id);
	}

}
