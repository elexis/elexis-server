package es.fhir.rest.core.transformer;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Encounter;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFindingsService;
import es.fhir.rest.core.IFhirTransformer;

@Component
public class EncounterIEncounterTransformer implements IFhirTransformer<Encounter, IEncounter> {

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public Optional<Encounter> getFhirObject(IEncounter localObject) {

		return null;
	}

	@Override
	public Optional<IEncounter> getLocalObject(Encounter fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IEncounter> updateLocalObject(Encounter fhirObject, IEncounter localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<IEncounter> createLocalObject(Encounter fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Encounter.class.equals(fhirClazz) && IEncounter.class.equals(localClazz);
	}

}
