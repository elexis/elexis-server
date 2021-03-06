package es.fhir.rest.core.model.util.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IXidService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.IContactHelper;
import info.elexis.server.core.connector.elexis.services.UserService;

@Component
public class PractitionerIMandatorTransformer implements IFhirTransformer<Practitioner, IMandator> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IXidService xidService;
	
	private IContactHelper contactHelper;
	
	@Activate
	private void activate(){
		contactHelper = new IContactHelper(modelService, xidService);
	}
	
	@Override
	public Optional<Practitioner> getFhirObject(IMandator localObject,SummaryEnum summaryEnum, Set<Include> includes){
		Practitioner practitioner = new Practitioner();
		
		practitioner.setId(new IdDt("Practitioner", localObject.getId()));
		
		List<Identifier> identifiers = contactHelper.getIdentifiers(localObject);
		identifiers.add(getElexisObjectIdentifier(localObject));
		practitioner.setIdentifier(identifiers);
		
		if (localObject.isPerson()) {
			IPerson mandatorPerson = modelService.load(localObject.getId(), IPerson.class).get();
			practitioner.setName(contactHelper.getHumanNames(mandatorPerson));
			practitioner.setGender(contactHelper.getGender(mandatorPerson.getGender()));
			practitioner.setBirthDate(contactHelper.getBirthDate(mandatorPerson));
			
			Optional<IUser> userLocalObject = UserService.findByContact(mandatorPerson);
			if (userLocalObject.isPresent()) {
				practitioner.setActive(userLocalObject.get().isActive());
			}
		}
		
		practitioner.setAddress(contactHelper.getAddresses(localObject));
		practitioner.setTelecom(contactHelper.getContactPoints(localObject));
		
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
