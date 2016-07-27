package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import ch.elexis.core.model.issue.Visibility;

@Converter(autoApply = true)
public class VisibilityConverter implements AttributeConverter<Visibility, String> {

	@Override
	public String convertToDatabaseColumn(Visibility prio) {
		if (prio == null) {
			return null;
		}
		return Integer.toString(prio.numericValue());
	}

	@Override
	public Visibility convertToEntityAttribute(String numValue) {
		return Visibility.byNumericSafe(numValue);
	}

}
