package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import info.elexis.server.core.connector.elexis.jpa.ProvidedEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;

public class ElexisDBStoreToStringConverter implements Converter {

	private static final long serialVersionUID = 7036321998248212269L;

	private Logger log = LoggerFactory.getLogger(ElexisDBStoreToStringConverter.class);

	private static final BidiMap<String, Class<? extends AbstractDBObject>> stsClassBidiMap;
	
	static {
		stsClassBidiMap = new DualHashBidiMap<String, Class<? extends AbstractDBObject>>();
		stsClassBidiMap.put("ch.artikelstamm.elexis.common.ArtikelstammItem", ArtikelstammItem.class);
		// TODO add other values
	}

	@Override
	public Object convertObjectValueToDataValue(Object objectValue, Session session) {
		if(!(objectValue instanceof AbstractDBObject)) {
			log.warn(" {} is not an AbstractDBObject", objectValue.getClass());
			return null;
		}
		
		AbstractDBObject adbo = (AbstractDBObject) objectValue;
		String classKey = stsClassBidiMap.getKey(objectValue.getClass());
		if(classKey==null) {
			log.warn("Could not resolve {} to storeToString name", objectValue.getClass());
			return null;
		}
		
		return classKey+StringConstants.DOUBLECOLON+adbo.getId();
	}

	@Override
	public Object convertDataValueToObjectValue(Object dataValue, Session session) {
		String dbData = (String) dataValue;
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

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {}


}
