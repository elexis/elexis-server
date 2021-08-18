package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IUserService;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;

@Component
public class PractitionerResourceProvider implements IFhirResourceProvider {
	
	@Reference(target="("+IModelService.SERVICEMODELNAME+"=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Reference
	private IUserService userService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Practitioner.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Practitioner, IMandator> getTransformer(){
		return (IFhirTransformer<Practitioner, IMandator>) transformerRegistry
			.getTransformerFor(Practitioner.class, IMandator.class);
	}
	
	@Read
	public Practitioner getResourceById(@IdParam IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IMandator> practitioner = modelService.load(idPart, IMandator.class);
			if (practitioner.isPresent()) {
				Optional<Practitioner> fhirPractitioner =
					getTransformer().getFhirObject(practitioner.get());
				return fhirPractitioner.get();
			}
		}
		return null;
	}
	
	@Search()
	public List<Practitioner> findPractitioner(
		@RequiredParam(name = Practitioner.SP_NAME) String name){
		if (name != null) {
			IQuery<IMandator> query = modelService.getQuery(IMandator.class);
			query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE,
				"%" + name + "%", true);
			query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE,
				"%" + name + "%", true);
			List<IMandator> practitioners = query.execute();
			if (!practitioners.isEmpty()) {
				// only Kontakt with existing user entry
				practitioners = practitioners.stream()
					.filter(contact -> !userService.getUsersByAssociatedContact(contact).isEmpty())
					.collect(Collectors.toList());
				List<Practitioner> ret = new ArrayList<Practitioner>();
				for (IMandator practitioner : practitioners) {
					Optional<Practitioner> fhirPractitioner =
						getTransformer().getFhirObject(practitioner);
					fhirPractitioner.ifPresent(fp -> ret.add(fp));
				}
				return ret;
			}
		}
		return Collections.emptyList();
	}
}
