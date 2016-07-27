package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import ch.elexis.core.model.issue.Type;

@Converter(autoApply = true)
public class TypeConverter implements AttributeConverter<Type, String> {

	@Override
	public String convertToDatabaseColumn(Type prio) {
		if (prio == null) {
			return null;
		}
		return Integer.toString(prio.numericValue());
	}

	@Override
	public Type convertToEntityAttribute(String numValue) {
		return Type.byNumericSafe(numValue);
	}

}
