package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Converter(autoApply = true)
public class LocalTimeConverterSafe implements AttributeConverter<LocalTime, String> {

	private Logger log = LoggerFactory.getLogger(LocalTimeConverterSafe.class);

	private final DateTimeFormatter HHmmss = DateTimeFormatter.ofPattern("HHmmss");

	@Override
	public String convertToDatabaseColumn(LocalTime date) {
		if (date == null) {
			return "000000";
		}

		return date.format(HHmmss);
	}

	@Override
	public LocalTime convertToEntityAttribute(String dateValue) {
		if (dateValue == null || dateValue.length() == 0) {
			return LocalTime.MIDNIGHT;
		}

		try {
			return LocalTime.parse(dateValue, HHmmss);
		} catch (DateTimeParseException e) {
			log.warn("Error parsing [{}], returning midnight.", dateValue, e);
		}
		return LocalTime.MIDNIGHT;
	}

}
