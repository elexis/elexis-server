package info.elexis.server.findings.fhir.jpa.codes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.util.model.TransientCoding;
import info.elexis.server.findings.fhir.jpa.model.annotated.LocalCoding;

public class LocalCodingModelAdapter implements ILocalCoding {

	private static String MAPPED_SEPARATOR = "||";
	private static String MAPPED_SEPARATOR_SPLITTER = "\\|\\|";
	private static String MAPPED_FIELD_SEPARATOR = "^";

	private LocalCoding localCoding;

	public LocalCodingModelAdapter(LocalCoding localCoding) {
		this.localCoding = localCoding;
	}

	@Override
	public String getCode() {
		return localCoding.getCode();
	}

	@Override
	public String getDisplay() {
		return localCoding.getDisplay();
	}

	@Override
	public String getSystem() {
		return CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem();
	}

	@Override
	public List<ICoding> getMappedCodes() {
		String mappedString = localCoding.getMapped();
		if (mappedString != null && !mappedString.isEmpty()) {
			return getMappedCodingFromString(mappedString);
		}
		return Collections.emptyList();
	}

	private List<ICoding> getMappedCodingFromString(String encoded) {
		String[] codeStrings = encoded.split(MAPPED_SEPARATOR_SPLITTER);
		if (codeStrings != null && codeStrings.length > 0) {
			List<ICoding> ret = new ArrayList<>();
			for (String string : codeStrings) {
				getCodingFromString(string).ifPresent(c -> ret.add(c));
			}
			return ret;
		}
		return Collections.emptyList();
	}

	@Override
	public void setMappedCodes(List<ICoding> mappedCodes) {
		String encoded = "";
		if (mappedCodes != null && !mappedCodes.isEmpty()) {
			encoded = getMappedCodingAsString(mappedCodes);
		}
		localCoding.setMapped(encoded);
	}

	private String getMappedCodingAsString(List<ICoding> mappedCoding) {
		StringBuilder sb = new StringBuilder();
		for (ICoding iCoding : mappedCoding) {
			if (sb.length() > 0) {
				sb.append(MAPPED_SEPARATOR);
			}
			sb.append(getAsString(iCoding));
		}
		return sb.toString();
	}

	private String getAsString(ICoding coding) {
		return coding.getSystem() + MAPPED_FIELD_SEPARATOR + coding.getCode() + MAPPED_FIELD_SEPARATOR
				+ coding.getDisplay();
	}

	private Optional<ICoding> getCodingFromString(String encoded) {
		String[] codingParts = encoded.split("\\" + MAPPED_FIELD_SEPARATOR);
		if (codingParts != null && codingParts.length > 1) {
			if (codingParts.length == 2) {
				return Optional.of(new TransientCoding(codingParts[0], codingParts[1], ""));
			} else if (codingParts.length == 3) {
				return Optional.of(new TransientCoding(codingParts[0], codingParts[1], codingParts[2]));
			}
		}
		return Optional.empty();
	}

	public void setCode(String code) {
		localCoding.setCode(code);
	}

	public void setDisplay(String display) {
		localCoding.setDisplay(display);
	}

	public LocalCoding getModel() {
		return localCoding;
	}
}
