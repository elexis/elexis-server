package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.StringTool;

public class IntegerStringConverter implements Converter {

	private static final long serialVersionUID = -2844423863068328997L;
	private Logger log = LoggerFactory.getLogger(IntegerStringConverter.class);
	
	@Override
	public String convertObjectValueToDataValue(Object objectValue, Session session) {
		return Integer.toString((int) objectValue);
	}

	@Override
	public Integer convertDataValueToObjectValue(Object dataValue, Session session) {
		if (StringTool.isNothing(dataValue)) {
			return 0;
		}
		try {
			return Integer.parseInt(((String) dataValue).trim());
		} catch (NumberFormatException ex) {
			log.warn("Number format exception "+dataValue, ex);
			return 0;
		}
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {}

}
