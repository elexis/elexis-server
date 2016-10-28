package es.fhir.rest.core.model.util.transformer;

import org.hl7.fhir.dstu3.model.EnumFactory;

import ch.elexis.core.model.prescription.EntryType;

@SuppressWarnings("serial")
public class PrescriptionEntryTypeFactory implements EnumFactory<EntryType> {

	@Override
	public EntryType fromCode(String codeString) throws IllegalArgumentException {
		return EntryType.valueOf(codeString);
	}

	@Override
	public String toCode(EntryType code) {
		return code.toString();
	}

	@Override
	public String toSystem(EntryType code) {
		return "www.elexis.info/prescription/entrytype";
	}

}
