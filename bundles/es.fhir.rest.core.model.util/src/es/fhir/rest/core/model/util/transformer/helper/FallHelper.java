package es.fhir.rest.core.model.util.transformer.helper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.model.FallConstants;
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

	public void setBin(Fall fall, String bin) {
		String billingMethod = fall.getExtInfoAsString(FallConstants.FLD_EXTINFO_BILLING);
		if (billingMethod != null && !billingMethod.isEmpty()) {
			if (billingMethod.equals("UVG")) {
				fall.setExtInfoValue("Unfallnummer", bin);
			} else {
				fall.setExtInfoValue("Versicherungsnummer", bin);
			}
		}
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
				Optional<Kontakt> kostenTrOpt = KontaktService.load(kostenTrId);
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
		if (startDate != null) {
			period.setStart(getDate(startDate.atStartOfDay()));
		}
		LocalDate endDate = fall.getDatumBis();
		if (endDate != null) {
			period.setEnd(getDate(endDate.atStartOfDay()));
		}
		return period;
	}

	public void setPeriod(Fall fall, Period period) {
		if (period.getStart() != null) {
			fall.setDatumVon(getLocalDateTime(period.getStart()).toLocalDate());
		}
	}

	public String getFallText(Fall fall) {
		DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$
		String grund = fall.getGrund();
		String bezeichnung = fall.getBezeichnung();
		LocalDate dateFrom = fall.getDatumVon();
		LocalDate dateTo = fall.getDatumBis();
		String billingSystem = fall.getExtInfoAsString(FallConstants.FLD_EXTINFO_BILLING);

		StringBuilder ret = new StringBuilder();
		if (dateTo != null) {
			ret.append("-GESCHLOSSEN-");
		}
		ret.append(billingSystem).append(": ").append(grund).append(" - "); //$NON-NLS-1$ //$NON-NLS-2$
		ret.append(bezeichnung).append("("); //$NON-NLS-1$
		String ed;
		if (dateTo == null) {
			ed = "offen";
		} else {
			ed = dateTo.format(dateFormat);
		}
		ret.append(dateFrom.format(dateFormat)).append("-").append(ed).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		return ret.toString();
	}

	public Optional<Coding> getType(Fall fall) {
		String billingSystem = fall.getExtInfoAsString(FallConstants.FLD_EXTINFO_BILLING);
		if (billingSystem != null) {
			Coding ret = new Coding();
			ret.setSystem(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem());
			ret.setCode(billingSystem);
			return Optional.of(ret);
		}
		return null;
	}

	public Optional<String> getType(Coverage fhirObject) {
		Coding fhirType = fhirObject.getType();
		if (fhirType.getSystem().equals(CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem())) {
			return Optional.ofNullable(fhirType.getCode());
		}
		return Optional.empty();
	}
}
