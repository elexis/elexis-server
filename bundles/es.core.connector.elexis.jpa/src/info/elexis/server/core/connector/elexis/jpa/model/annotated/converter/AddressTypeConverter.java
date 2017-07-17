package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import ch.elexis.core.types.AddressType;

@Converter(autoApply = true)
public class AddressTypeConverter implements AttributeConverter<AddressType, String> {

	@Override
	public String convertToDatabaseColumn(AddressType addressType) {
		if (addressType == null) {
			return null;
		}
		return Integer.toString(addressType.getValue());
	}

	@Override
	public AddressType convertToEntityAttribute(String addressTypeString) {
		if (addressTypeString == null) {
			return null;
		}
		return AddressType.get(Integer.valueOf(addressTypeString));
	}

}
