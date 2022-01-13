package es.fhir.rest.core.resources;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Person;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import es.fhir.rest.core.resources.util.IContactSearchFilterQueryAdapter;
import es.fhir.rest.core.resources.util.QueryUtil;

@Component
public class PersonResourceProvider implements IFhirResourceProvider {
	
	private Logger log;
	private ResourceProviderUtil resourceProviderUtil;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Person.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Person, IPerson> getTransformer(){
		return (IFhirTransformer<Person, IPerson>) transformerRegistry
			.getTransformerFor(Person.class, IPerson.class);
	}
	
	@Activate
	public void activate(){
		log = LoggerFactory.getLogger(getClass());
		resourceProviderUtil = new ResourceProviderUtil();
	}
	
	@Create
	public MethodOutcome create(@ResourceParam Person patient){
		return resourceProviderUtil.createResource(getTransformer(), patient, log);
	}
	
	@Read
	public Person read(@IdParam IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IPerson> person = modelService.load(idPart, IPerson.class);
			if (person.isPresent()) {
				Optional<Person> fhirPerson = getTransformer().getFhirObject(person.get());
				return fhirPerson.get();
				
			}
		}
		return null;
	}
	
	@Update
	public MethodOutcome update(@IdParam IdType theId, @ResourceParam Person patient){
		// TODO request lock or fail
		return resourceProviderUtil.updateResource(theId, getTransformer(), patient, log);
	}
	
	@Delete
	public void delete(@IdParam IdType theId){
		// TODO request lock or fail
		if (theId != null) {
			Optional<IPerson> resource = modelService.load(theId.getIdPart(), IPerson.class);
			if (!resource.isPresent()) {
				throw new ResourceNotFoundException(theId);
			}
			modelService.delete(resource.get());
		}
	}
	
	@Search
	public List<Person> search(@OptionalParam(name = Person.SP_NAME) StringParam name,
		@OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_FILTER) StringAndListParam theFtFilter){
		
		IQuery<IPerson> query = modelService.getQuery(IPerson.class);
		
		if (name != null) {
			QueryUtil.andContactNameCriterion(query, name);
		}
		
		if (theFtFilter != null) {
			new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		}
		
		List<IPerson> persons = query.execute();
		List<Person> _persons =
			persons.parallelStream().map(org -> getTransformer().getFhirObject(org))
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		return _persons;
	}
	
	//	@Search()
	//	public List<Organization> findOrganization(
	//		@RequiredParam(name = Organization.SP_NAME) String name){
	//		if (name != null) {
	//			IQuery<IOrganization> query = modelService.getQuery(IOrganization.class);
	//			query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE,
	//				"%" + name + "%", true);
	//			query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE,
	//				"%" + name + "%", true);
	//			List<IOrganization> organizations = query.execute();
	//			if (!organizations.isEmpty()) {
	//				List<Organization> ret = new ArrayList<>();
	//				for (IOrganization organization : organizations) {
	//					Optional<Organization> fhirOrganization =
	//						getTransformer().getFhirObject(organization);
	//					fhirOrganization.ifPresent(ret::add);
	//				}
	//				return ret;
	//			}
	//		}
	//		return Collections.emptyList();
	//	}
	//	
	//	@Search()
	//	public List<Organization> findOrganizationByFilter(
	//		@RequiredParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_FILTER) StringAndListParam theFtFilter,
	//		@OptionalParam(name = Organization.SP_ACTIVE) TokenParam isActive, @Sort SortSpec theSort,
	//		SummaryEnum theSummary){
	//		
	//		IQuery<IOrganization> query = modelService.getQuery(IOrganization.class);
	//		new PatientSearchFilterQueryAdapter().adapt(query, theFtFilter);
	//		List<IOrganization> organizations = query.execute();
	//		List<Organization> ret = organizations.parallelStream()
	//			.map(org -> getTransformer().getFhirObject(org, theSummary, Collections.emptySet()))
	//			.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	//		return ret;
	//	}
}
