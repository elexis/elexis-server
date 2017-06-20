package info.elexis.server.findings.fhir.jpa.model.annotated.converter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

import ch.elexis.core.constants.StringConstants;

public class BooleanCharacterConverterSafe implements Converter {

	private static final long serialVersionUID = 4230568260398475922L;

	@Override
	public String convertObjectValueToDataValue(Object objectValue, Session session) {
		return (Boolean.valueOf(objectValue.toString()) == true) ? StringConstants.ONE : StringConstants.ZERO;
	}

	@Override
	public Boolean convertDataValueToObjectValue(Object dataValue, Session session) {
		String value = (String) dataValue;
		if (StringUtils.isEmpty(value)) {
			return false;
		}
		return (value.equals(StringConstants.ONE)) ? true : false;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {
	}

}
