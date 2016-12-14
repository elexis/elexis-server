package es.fhir.rest.core.model.util.transformer.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.util.ModelUtil;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;

public class AbstractHelper {

	protected Date getDate(LocalDateTime localDateTime) {
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}

	protected Date getDate(LocalDate localDate) {
		ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}

	protected LocalDateTime getLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	public Reference getReference(String resourceType, AbstractDBObjectIdDeleted dbObject) {
		return new Reference(new IdDt("Patient", dbObject.getId()));
	}

	public void setText(DomainResource domainResource, String text) {
		Narrative narrative = domainResource.getText();
		if (narrative == null) {
			narrative = new Narrative();
		}
		String divEncodedText = text.replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("&", "&amp;")
				.replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		domainResource.setText(narrative);
	}

	public Optional<String> getText(DomainResource domainResource) {
		Narrative narrative = domainResource.getText();
		if (narrative != null && narrative.getDivAsString() != null) {
			return ModelUtil.getNarrativeAsString(narrative);
		}
		return Optional.empty();
	}
}
