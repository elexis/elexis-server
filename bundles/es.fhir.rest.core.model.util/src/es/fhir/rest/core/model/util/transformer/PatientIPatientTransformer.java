package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Patient;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.IContactHelper;
import es.fhir.rest.core.model.util.transformer.mapper.IPatientPatientAttributeMapper;

@Component(property = IFhirTransformer.TRANSFORMERID + "=Patient.IPatient")
public class PatientIPatientTransformer implements IFhirTransformer<Patient, IPatient> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	private IPatientPatientAttributeMapper attributeMapper;

	@Activate
	private void activate() {
		IContactHelper contactHelper = new IContactHelper(modelService);
		attributeMapper = new IPatientPatientAttributeMapper(contactHelper);
	}

	@Override
	public Optional<Patient> getFhirObject(IPatient localObject, Set<Include> includes) {
		Patient patient = new Patient();
		attributeMapper.elexisToFhir(localObject, patient);
		return Optional.of(patient);
	}

	@Override
	public Optional<IPatient> getLocalObject(Patient fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return modelService.load(id, IPatient.class);
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Patient.class.equals(fhirClazz) && IPatient.class.equals(localClazz);
	}

	@Override
	public Optional<IPatient> updateLocalObject(Patient fhirObject, IPatient localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IPatient> createLocalObject(Patient fhirObject) {
		IPatient create = modelService.create(IPatient.class);
		attributeMapper.fhirToElexis(fhirObject, create);
		modelService.save(create);
		return Optional.of(create);
	}
}
