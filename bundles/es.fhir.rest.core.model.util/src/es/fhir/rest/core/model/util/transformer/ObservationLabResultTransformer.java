package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Observation;
import org.osgi.service.component.annotations.Component;

import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

@Component
public class ObservationLabResultTransformer implements IFhirTransformer<Observation, LabResult> {

	@Override
	public Optional<Observation> getFhirObject(LabResult localObject) {
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

}
