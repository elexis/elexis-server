package es.fhir.rest.core.model.util.transformer;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.PractitionerRole;
import org.hl7.fhir.dstu3.model.Reference;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.MandantHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;

@Component
public class PractitionerRoleUserTransformer implements IFhirTransformer<PractitionerRole, User> {

	private MandantHelper mandantHelper = new MandantHelper();

	@Override
	public Optional<PractitionerRole> getFhirObject(User localObject, Set<Include> includes) {
		PractitionerRole practitionerRole = new PractitionerRole();
		practitionerRole.setId(new IdDt("PractitionerRole", localObject.getId()));

		Collection<Role> roles = localObject.getRoles();
		for (Role role : roles) {
			String roleId = role.getId();
			if (roleId != null) {
				practitionerRole.addCode(mandantHelper.getPractitionerRoleCode(roleId));
			}
		}
		practitionerRole.setActive(localObject.isActive());
		// add the practitioner
		if(localObject.getKontakt() != null) {
			practitionerRole.setPractitioner(
					new Reference(new IdDt(Practitioner.class.getSimpleName(), localObject.getKontakt().getId())));
		}
		localObject.getKontakt().getId();

		return Optional.of(practitionerRole);
	}

	@Override
	public Optional<User> getLocalObject(PractitionerRole fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<User> updateLocalObject(PractitionerRole fhirObject, User localObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<User> createLocalObject(PractitionerRole fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return PractitionerRole.class.equals(fhirClazz) && User.class.equals(localClazz);
	}
}
