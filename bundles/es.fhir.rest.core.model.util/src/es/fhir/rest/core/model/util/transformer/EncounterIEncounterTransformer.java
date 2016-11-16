package es.fhir.rest.core.model.util.transformer;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.findings.IEncounter;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.FindingsContentHelper;

@Component
public class EncounterIEncounterTransformer implements IFhirTransformer<Encounter, IEncounter> {

	private FindingsContentHelper contentHelper = new FindingsContentHelper();

	@Override
	public Optional<Encounter> getFhirObject(IEncounter localObject) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((Encounter) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> getLocalObject(Encounter fhirObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> updateLocalObject(Encounter fhirObject, IEncounter localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IEncounter> createLocalObject(Encounter fhirObject) {
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Encounter.class.equals(fhirClazz) && IEncounter.class.equals(localClazz);
	}

}
