package es.fhir.rest.core.resources.util;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeType;

public class CodeTypeUtil {

	public static Optional<String> getSystem(CodeType roleCode) {
		String codeValue = roleCode.getValue();
		if (codeValue != null) {
			String[] parts = codeValue.split("\\|");
			if (parts.length == 2) {
				return Optional.of(parts[0]);
			}
		}
		return Optional.empty();
	}

	public static Optional<String> getCode(CodeType roleCode) {
		String codeValue = roleCode.getValue();
		if (codeValue != null) {
			String[] parts = codeValue.split("\\|");
			if (parts.length == 2) {
				return Optional.of(parts[1]);
			} else if (parts.length == 1) {
				return Optional.of(parts[0]);
			}
		}
		return Optional.empty();
	}

}
