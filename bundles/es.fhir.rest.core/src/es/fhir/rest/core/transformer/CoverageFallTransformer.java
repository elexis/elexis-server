package es.fhir.rest.core.transformer;

import java.time.LocalDate;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Coverage;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

@Component
public class CoverageFallTransformer implements IFhirTransformer<Coverage, Fall> {

	@Override
	public Optional<Coverage> getFhirObject(Fall localObject) {
		Coverage coverage = new Coverage();

		coverage.setId(new IdDt("Coverage", localObject.getId()));

		Identifier elexisId = coverage.addIdentifier();
		elexisId.setSystem("www.elexis.info/objid");
		elexisId.setValue(localObject.getId());

		String coverageNumber = localObject.getVersNummer();
		if (coverageNumber != null) {
			Identifier coverageId = coverage.addIdentifier();
			coverageId.setSystem("http://www.elexis.info/coverage");
			coverageId.setValue(coverageNumber);
		}

		Kontakt patient = localObject.getPatientKontakt();
		if (patient != null) {
			Reference reference = new Reference(new IdDt("Patient", patient.getId()));
			coverage.setBeneficiary(reference);
		}

		Kontakt kostenTr = localObject.getKostentrKontakt();
		if (kostenTr != null) {
			Reference reference = null;
			if(kostenTr.isOrganisation()) {
				reference = new Reference(new IdDt("Organization", kostenTr.getId()));
			} else if (kostenTr.isPatient()) {
				reference = new Reference(new IdDt("Patient", kostenTr.getId()));
			}
			coverage.setIssuer(reference);
		}

		Period period = new Period();
		LocalDate startDate = localObject.getDatumVon();
		if(startDate != null) {
			period.setStart(getDate(startDate.atStartOfDay()));
		}
		LocalDate endDate = localObject.getDatumBis();
		if(endDate != null) {
			period.setEnd(getDate(endDate.atStartOfDay()));
		}
		coverage.setPeriod(period);

		return Optional.of(coverage);
	}

	@Override
	public Optional<Fall> getLocalObject(Coverage fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Fall> updateLocalObject(Coverage fhirObject, Fall localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Fall> createLocalObject(Coverage fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Coverage.class.equals(fhirClazz) && Fall.class.equals(localClazz);
	}

}
