package es.fhir.rest.core.model.util.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Organization;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.IModelService;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.IContactHelper;

@Component
public class OrganizationKontaktTransformer
		implements IFhirTransformer<Organization, IOrganization> {
	
	@Reference(target="("+IModelService.SERVICEMODELNAME+"=ch.elexis.core.model)")
	private IModelService modelService;
	
	private IContactHelper contactHelper;
	
	@Activate
	public void activate() {
		contactHelper = new IContactHelper(modelService);
	}
	
	@Override
	public Optional<Organization> getFhirObject(IOrganization localObject, Set<Include> includes){
		Organization organization = new Organization();
		
		organization.setId(new IdDt("Organization", localObject.getId()));
		
		List<Identifier> identifiers = contactHelper.getIdentifiers(localObject);
		identifiers.add(getElexisObjectIdentifier(localObject));
		organization.setIdentifier(identifiers);
		
		organization.setName(contactHelper.getOrganizationName(localObject));
		List<Address> addresses = contactHelper.getAddresses(localObject);
		for (Address address : addresses) {
			organization.addAddress(address);
		}
		List<ContactPoint> contactPoints = contactHelper.getContactPoints(localObject);
		for (ContactPoint contactPoint : contactPoints) {
			organization.addTelecom(contactPoint);
		}
		
		return Optional.of(organization);
	}
	
	@Override
	public Optional<IOrganization> getLocalObject(Organization fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<IOrganization> updateLocalObject(Organization fhirObject,
		IOrganization localObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Optional<IOrganization> createLocalObject(Organization fhirObject){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz){
		return Organization.class.equals(fhirClazz) && IOrganization.class.equals(localClazz);
	}
	
}
