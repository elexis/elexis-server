package es.fhir.rest.core.transformer;

import java.util.Optional;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

@Component
public class OrganizationKontaktTransformer implements IFhirTransformer<Organization, Kontakt> {

	@Override
	public Optional<Organization> getFhirObject(Kontakt localObject) {
		Organization organization = new Organization();

		organization.setId(new IdDt("Organization", localObject.getId()));
		Identifier elexisId = organization.addIdentifier();

		elexisId.setSystem("www.elexis.info/objid");
		elexisId.setValue(localObject.getId());

		organization.setName(localObject.getLabel());

		return Optional.of(organization);
	}

	@Override
	public Optional<Kontakt> getLocalObject(Organization fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Kontakt> updateLocalObject(Organization fhirObject, Kontakt localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Kontakt> createLocalObject(Organization fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Organization.class.equals(fhirClazz) && Kontakt.class.equals(localClazz);
	}

}
