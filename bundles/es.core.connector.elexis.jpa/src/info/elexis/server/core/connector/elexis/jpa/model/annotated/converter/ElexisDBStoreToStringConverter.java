package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import java.util.Optional;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;

public class ElexisDBStoreToStringConverter implements Converter {

	private static final long serialVersionUID = 7036321998248212269L;

	private Logger log = LoggerFactory.getLogger(ElexisDBStoreToStringConverter.class);

	@Override
	public Object convertObjectValueToDataValue(Object objectValue, Session session) {
		if (objectValue == null) {
			return null;
		}

		if (!(objectValue instanceof AbstractDBObjectIdDeleted)) {
			log.warn(" [{}] is not an AbstractDBObject", objectValue.getClass());
			return null;
		}

		return StoreToStringService.storeToString((AbstractDBObjectIdDeleted) objectValue);
	}

	@Override
	public Object convertDataValueToObjectValue(Object dataValue, Session session) {
		if (dataValue == null) {
			return null;
		}
		Optional<AbstractDBObjectIdDeleted> object = StoreToStringService.INSTANCE
				.createDetachedFromString((String) dataValue);
		if (object.isPresent()) {
			return object.get();
		}
		log.warn("Could not create object from store to string [{}]. ", dataValue);
		return null;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {
	}

}
