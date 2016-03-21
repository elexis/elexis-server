package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElexisDBStringDateConverter implements Converter {

	private Logger log = LoggerFactory.getLogger(ElexisDBStringDateConverter.class);
	private static final long serialVersionUID = 9168830092897672615L;

	private final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Override
	public LocalDate convertDataValueToObjectValue(Object dataValue, Session arg1) {
		if (dataValue == null) {
			return null;
		}
		String dateString = dataValue.toString();

		// TODO unset has to be represented with null, that is ""
		if (dateString == null || dateString.length() == 0)
			return null;
		try {
			return LocalDate.parse(dateString, yyyyMMdd);
		} catch (DateTimeParseException e) {
			log.warn("Error parsing {}", dateString, e);
		}
		return null;
	}

	@Override
	public String convertObjectValueToDataValue(Object objectValue, Session arg1) {
		if (objectValue == null)
			return null;
		LocalDate date = (LocalDate) objectValue;
		return date.format(yyyyMMdd);
	}

	@Override
	public void initialize(DatabaseMapping arg0, Session arg1) {
	}

	@Override
	public boolean isMutable() {
		// TODO Auto-generated method stub
		return false;
	}

}
