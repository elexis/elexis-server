package es.fhir.rest.core.resources;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Organization;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import es.fhir.rest.core.resources.util.IContactSearchFilterQueryAdapter;
import es.fhir.rest.core.resources.util.QueryUtil;

@Component(service = IFhirResourceProvider.class)
public class OrganizationResourceProvider
		extends AbstractFhirCrudResourceProvider<Organization, IOrganization> {
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	protected IModelService coreModelService;
	
	@Reference
	private ILocalLockService localLockService;
	
	public OrganizationResourceProvider(){
		super(IOrganization.class);
	}
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Organization.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Organization, IOrganization> getTransformer(){
		return (IFhirTransformer<Organization, IOrganization>) transformerRegistry
			.getTransformerFor(Organization.class, IOrganization.class);
	}
	
	@Activate
	public void activate() {
		setCoreModelService(coreModelService);
		setLocalLockService(localLockService);
	}
	
	@Search
	public List<Organization> search(@OptionalParam(name = Organization.SP_NAME) StringParam name,
		@OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_FILTER) StringAndListParam theFtFilter){
		
		IQuery<IOrganization> query = coreModelService.getQuery(IOrganization.class);
		
		if (name != null) {
			QueryUtil.andContactNameCriterion(query, name);
		}
		
		if (theFtFilter != null) {
			new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		}
		
		// TODO default limit result number
		List<IOrganization> organizations = query.execute();
		List<Organization> _organizations =
			organizations.parallelStream().map(org -> getTransformer().getFhirObject(org))
				.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		return _organizations;
	}

	
}
