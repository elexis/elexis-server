package es.fhir.rest.core.resources.util;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;
import info.elexis.server.core.connector.elexis.services.LabResultService;

public class CodeTypeUtil {

	public static Optional<String> getSystem(CodeType code) {
		String codeValue = code.getValue();
		if (codeValue != null) {
			String[] parts = codeValue.split("\\|");
			if (parts.length == 2) {
				return Optional.of(parts[0]);
			}
		}
		return Optional.empty();
	}

	public static Optional<String> getCode(CodeType code) {
		String codeValue = code.getValue();
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

	public static boolean isVitoLabkey(Observation observation, String codeString) {
		String labresultId = observation.getIdElement().getIdPart();
		Optional<LabResult> result = LabResultService.load(labresultId);
		if (result.isPresent()) {
			LabItem item = result.get().getItem();
			if (item != null) {
				String export = item.getExport();
				if (export.startsWith("vitolabkey:")) {
					String[] parts = export.split(":");
					if (parts.length == 2) {
						parts = parts[1].split(",");
						if (parts.length > 0) {
							for (String string : parts) {
								if (string.equals(codeString)) {
									return true;
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean isCodeInConcept(CodeableConcept concept, String system, String code) {
		List<Coding> codings = concept.getCoding();
		for (Coding coding : codings) {
			if (coding.getSystem().equals(system) && coding.getCode().equals(code)) {
				return true;
			}
		}
		return false;
	}
}
