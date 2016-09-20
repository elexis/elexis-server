package info.elexis.server.findings.fhir.jpa.model.helper;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.parser.IParser;
import ch.elexis.core.findings.IFinding;

public class FhirHelper {
	private static FhirContext context = FhirContext.forDstu3();

	private IParser parser;

	public IParser getJsonParser() {
		if (parser == null) {
			parser = context.newJsonParser();
		}
		return parser;
	}

	public Optional<IBaseResource> loadResource(IFinding finding) throws DataFormatException {
		IBaseResource resource = null;
		if (finding.getRawContent() != null && !finding.getRawContent().isEmpty()) {
			resource = getJsonParser().parseResource(finding.getRawContent());
		}
		return Optional.ofNullable(resource);
	}

	public void saveResource(IBaseResource resource, IFinding finding) throws DataFormatException {
		if (resource != null) {
			String resourceJson = getJsonParser().encodeResourceToString(resource);
			finding.setRawContent(resourceJson);
		}
	}
}
