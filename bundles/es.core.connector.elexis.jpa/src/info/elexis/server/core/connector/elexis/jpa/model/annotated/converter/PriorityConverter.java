package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import ch.elexis.core.model.issue.Priority;

@Converter(autoApply = true)
public class PriorityConverter implements AttributeConverter<Priority, String> {

	@Override
	public String convertToDatabaseColumn(Priority prio) {
		if (prio == null) {
			return null;
		}
		return Integer.toString(prio.numericValue());
	}

	@Override
	public Priority convertToEntityAttribute(String numValue) {
		return Priority.byNumericSafe(numValue);
	}

}
