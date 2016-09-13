package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.StringTool;

@Converter(autoApply = true)
public class FailsafeIntegerStringNullableConverter implements AttributeConverter<Integer, String> {

	private Logger log = LoggerFactory.getLogger(FailsafeIntegerStringNullableConverter.class);
	
	@Override
	public String convertToDatabaseColumn(Integer attribute) {
		if (attribute != null) {
			return attribute.toString();
		}
		return null;
	}

	@Override
	public Integer convertToEntityAttribute(String dbData) {
		if(StringTool.isNothing(dbData)) {
			return null;
		}
		try {
			return Integer.parseInt(dbData.trim());
		} catch (NumberFormatException nfe) {
			log.warn("Invalid int value [{}], returning null.", dbData, nfe);
		}
		return null;
	}

}
