package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import ch.elexis.core.model.issue.ProcessStatus;

@Converter(autoApply = true)
public class ProcessStatusConverter implements AttributeConverter<ProcessStatus, String> {

	@Override
	public String convertToDatabaseColumn(ProcessStatus prio) {
		if (prio == null) {
			return null;
		}
		return Integer.toString(prio.numericValue());
	}

	@Override
	public ProcessStatus convertToEntityAttribute(String numValue) {
		return ProcessStatus.byNumericSafe(numValue);
	}

}
