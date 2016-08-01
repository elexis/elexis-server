package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.InvoiceState;
import ch.rgw.tools.StringTool;

@Converter(autoApply = true)
public class InvoiceStateConverter implements AttributeConverter<InvoiceState, String> {

	private Logger log = LoggerFactory.getLogger(InvoiceStateConverter.class);

	@Override
	public String convertToDatabaseColumn(InvoiceState attribute) {
		if (attribute == null) {
			return Integer.toString(InvoiceState.UNBEKANNT.numericValue());
		}
		return Integer.toString(attribute.getState());
	}

	@Override
	public InvoiceState convertToEntityAttribute(String dbData) {
		if (StringTool.isNothing(dbData)) {
			return InvoiceState.UNBEKANNT;
		}
		try {
			int value = Integer.parseInt(dbData.trim());
			return InvoiceState.fromState(value);
		} catch (NumberFormatException ex) {
			log.warn("Number format exception " + dbData, ex);
			return InvoiceState.UNBEKANNT;
		}
	}

}
