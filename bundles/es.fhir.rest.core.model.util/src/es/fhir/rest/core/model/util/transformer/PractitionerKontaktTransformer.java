package es.fhir.rest.core.model.util.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.useradmin.User;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IModelService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.IContactHelper;
import es.fhir.rest.core.model.util.transformer.helper.KontaktHelper;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.UserService;

@Component
public class PractitionerKontaktTransformer implements IFhirTransformer<Practitioner, IMandator> {
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private IModelService modelService;
	
	private IContactHelper contactHelper = new IContactHelper(modelService);
	
	@Override
	public Optional<Practitioner> getFhirObject(IMandator localObject, Set<Include> includes){
		Practitioner practitioner = new Practitioner();
		
		practitioner.setId(new IdDt("Practitioner", localObject.getId()));
		
		List<Identifier> identifiers = contactHelper.getIdentifiers(localObject);
		identifiers.add(getElexisObjectIdentifier(localObject));
		practitioner.setIdentifier(identifiers);
		
		practitioner.setName(contactHelper.getHumanNames(localObject));
		practitioner.setGender(contactHelper.getGender(localObject.getGender()));
		practitioner.setBirthDate(contactHelper.getBirthDate(localObject));
		practitioner.setAddress(contactHelper.getAddresses(localObject));
		practitioner.setTelecom(contactHelper.getContactPoints(localObject));
		
		Optional<User> userLocalObject = UserService.findByKontakt(localObject);
		if (userLocalObject.isPresent()) {
			practitioner.setActive(userLocalObject.get().isActive());
		}
		
		return Optional.of(practitioner);
	}
	
	@Override
	public Optional<IMandator> getLocalObject(Practitioner fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<IMandator> updateLocalObject(Practitioner fhirObject, IMandator localObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<IMandator> createLocalObject(Practitioner fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Practitioner.class.equals(fhirClazz) && IMandator.class.equals(localClazz);
	}
}
