package es.fhir.rest.core.model.util.transformer;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.KontaktHelper;
import es.fhir.rest.core.model.util.transformer.helper.MandantHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.services.UserService;

@Component
public class PractitionerKontaktTransformer implements IFhirTransformer<Practitioner, Kontakt> {

	private KontaktHelper kontaktHelper = new KontaktHelper();
	private MandantHelper mandantHelper = new MandantHelper();

	@Override
	public Optional<Practitioner> getFhirObject(Kontakt localObject) {
		Practitioner practitioner = new Practitioner();

		practitioner.setId(new IdDt("Practitioner", localObject.getId()));

		List<Identifier> identifiers = kontaktHelper.getIdentifiers(localObject);
		identifiers.add(getElexisObjectIdentifier(localObject));
		practitioner.setIdentifier(identifiers);

		practitioner.setName(kontaktHelper.getHumanNames(localObject));
		practitioner.setGender(kontaktHelper.getGender(localObject.getGender()));
		practitioner.setBirthDate(kontaktHelper.getBirthDate(localObject));
		practitioner.setAddress(kontaktHelper.getAddresses(localObject));
		practitioner.setTelecom(kontaktHelper.getContactPoints(localObject));

		Optional<User> userLocalObject = UserService.findByKontakt(localObject);
		if (userLocalObject.isPresent()) {
			Collection<Role> roles = userLocalObject.get().getRoles();
			for (Role role : roles) {
				String roleId = role.getId();
				if (roleId != null) {
					practitioner.addRole(mandantHelper.getPractitionerRoleComponent(roleId));
				}
			}
			practitioner.setActive(userLocalObject.get().isActive());
		}

		return Optional.of(practitioner);
	}

	@Override
	public Optional<Kontakt> getLocalObject(Practitioner fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Kontakt> updateLocalObject(Practitioner fhirObject, Kontakt localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Kontakt> createLocalObject(Practitioner fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Practitioner.class.equals(fhirClazz) && Kontakt.class.equals(localClazz);
	}
}
