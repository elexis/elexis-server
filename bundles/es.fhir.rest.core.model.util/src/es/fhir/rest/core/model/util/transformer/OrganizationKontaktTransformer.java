package es.fhir.rest.core.model.util.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.KontaktHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

@Component
public class OrganizationKontaktTransformer implements IFhirTransformer<Organization, Kontakt> {

	private KontaktHelper kontaktHelper = new KontaktHelper();

	@Override
	public Optional<Organization> getFhirObject(Kontakt localObject, Set<Include> includes) {
		Organization organization = new Organization();

		organization.setId(new IdDt("Organization", localObject.getId()));

		List<Identifier> identifiers = kontaktHelper.getIdentifiers(localObject);
		identifiers.add(getElexisObjectIdentifier(localObject));
		organization.setIdentifier(identifiers);

		organization.setName(kontaktHelper.getOrganizationName(localObject));
		List<Address> addresses = kontaktHelper.getAddresses(localObject);
		for (Address address : addresses) {
			organization.addAddress(address);
		}
		List<ContactPoint> contactPoints = kontaktHelper.getContactPoints(localObject);
		for (ContactPoint contactPoint : contactPoints) {
			organization.addTelecom(contactPoint);
		}

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
