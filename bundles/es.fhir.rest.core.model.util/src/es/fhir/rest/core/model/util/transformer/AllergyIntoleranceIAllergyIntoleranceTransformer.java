package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.AllergyIntolerance;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.model.api.Include;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.FindingsContentHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component(immediate = true)
public class AllergyIntoleranceIAllergyIntoleranceTransformer
		implements IFhirTransformer<AllergyIntolerance, IAllergyIntolerance> {

	private FindingsContentHelper contentHelper = new FindingsContentHelper();

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Optional<AllergyIntolerance> getFhirObject(IAllergyIntolerance localObject, Set<Include> includes){
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((AllergyIntolerance) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IAllergyIntolerance> getLocalObject(AllergyIntolerance fhirObject){
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<IAllergyIntolerance> existing =
				findingsService.findById(fhirObject.getId(), IAllergyIntolerance.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IAllergyIntolerance> updateLocalObject(AllergyIntolerance fhirObject,
		IAllergyIntolerance localObject){
		return Optional.empty();
	}

	@Override
	public Optional<IAllergyIntolerance> createLocalObject(AllergyIntolerance fhirObject){
		IAllergyIntolerance IallergyIntolerance = findingsService.create(IAllergyIntolerance.class);
		contentHelper.setResource(fhirObject, IallergyIntolerance);
		if (fhirObject.getPatient() != null && fhirObject.getPatient().hasReference()) {
			String id = fhirObject.getPatient().getReferenceElement().getIdPart();
			Optional<Kontakt> patient = KontaktService.load(id);
			patient.ifPresent(k -> IallergyIntolerance.setPatientId(id));
		}
		findingsService.saveFinding(IallergyIntolerance);
		return Optional.of(IallergyIntolerance);
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return AllergyIntolerance.class.equals(fhirClazz)
			&& IAllergyIntolerance.class.equals(localClazz);
	}

}
