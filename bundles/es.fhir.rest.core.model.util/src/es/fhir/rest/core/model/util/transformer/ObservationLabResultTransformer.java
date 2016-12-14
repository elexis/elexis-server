package es.fhir.rest.core.model.util.transformer;

import java.util.Collections;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.instance.model.valuesets.ObservationCategory;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.LabResultHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

@Component
public class ObservationLabResultTransformer implements IFhirTransformer<Observation, LabResult> {

	private LabResultHelper labResultHelper = new LabResultHelper();

	@Override
	public Optional<Observation> getFhirObject(LabResult localObject) {
		Observation observation = new Observation();

		observation.setId(new IdDt("Observation", localObject.getId()));
		observation.addIdentifier(getElexisObjectIdentifier(localObject));

		CodeableConcept observationCode = new CodeableConcept();
		observationCode
		.setCoding(Collections.singletonList(new Coding(ObservationCategory.LABORATORY.getSystem(),
				ObservationCategory.LABORATORY.toCode(), ObservationCategory.LABORATORY.getDisplay())));
		observation.addCategory(observationCode);
		
		observation.setSubject(labResultHelper.getReference("Patient", localObject.getPatient()));
		
		observation.setEffective(labResultHelper.getEffectiveDateTime(localObject));

		observation.setValue(labResultHelper.getResult(localObject));

		observation.setReferenceRange(labResultHelper.getReferenceComponents(localObject));

		observation.setCode(labResultHelper.getCodeableConcept(localObject));

		return Optional.of(observation);
	}

	@Override
	public Optional<LabResult> getLocalObject(Observation fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<LabResult> updateLocalObject(Observation fhirObject, LabResult localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<LabResult> createLocalObject(Observation fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Observation.class.equals(fhirClazz) && LabResult.class.equals(localClazz);
	}

}
