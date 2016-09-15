package es.fhir.rest.core.transformer.helper;

import java.time.LocalDate;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

public class FallHelper extends AbstractHelper {

	public String getBin(Fall fall) {
		String ret = fall.getVersNummer();
		if (ret == null) {
			ret = fall.getExtInfoAsString("Versicherungsnummer");
		}
		return ret;
	}

	public Reference getBeneficiaryReference(Fall fall) {
		Kontakt patient = fall.getPatientKontakt();
		if (patient != null) {
			return new Reference(new IdDt("Patient", patient.getId()));
		}
		return null;
	}

	public Reference getIssuerReference(Fall fall) {
		Kontakt kostenTr = fall.getKostentrKontakt();
		if (kostenTr == null) {
			String kostenTrId = fall.getExtInfoAsString("Kostenträger");
			if (kostenTrId != null && !kostenTrId.isEmpty()) {
				Optional<Kontakt> kostenTrOpt = KontaktService.INSTANCE.findById(kostenTrId);
				if (kostenTrOpt.isPresent()) {
					kostenTr = kostenTrOpt.get();
				}
			}
		}
		if (kostenTr != null) {
			if (kostenTr.isOrganisation()) {
				return new Reference(new IdDt("Organization", kostenTr.getId()));
			} else if (kostenTr.isPatient()) {
				return new Reference(new IdDt("Patient", kostenTr.getId()));
			}
		}
		return null;
	}

	public Period getPeriod(Fall fall) {
		Period period = new Period();
		LocalDate startDate = fall.getDatumVon();
		if(startDate != null) {
			period.setStart(getDate(startDate.atStartOfDay()));
		}
		LocalDate endDate = fall.getDatumBis();
		if(endDate != null) {
			period.setEnd(getDate(endDate.atStartOfDay()));
		}
		return period;
	}

}
