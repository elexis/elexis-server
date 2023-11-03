package es.fhir.rest.core.resources;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Practitioner;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IUserService;
import es.fhir.rest.core.resources.util.IContactSearchFilterQueryAdapter;
import es.fhir.rest.core.resources.util.QueryUtil;

@Component(service = IFhirResourceProvider.class)
public class PractitionerResourceProvider extends AbstractFhirCrudResourceProvider<Practitioner, IMandator> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	protected IModelService coreModelService;

	@Reference
	private IContextService contextService;

	@Reference
	private ILocalLockService localLockService;

	public PractitionerResourceProvider() {
		super(IMandator.class);
	}

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Reference
	private IUserService userService;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Practitioner.class;
	}

	@Activate
	public void activate() {
		setModelService(coreModelService);
		setLocalLockService(localLockService);
	}

	@Override
	public IFhirTransformer<Practitioner, IMandator> getTransformer() {
		return transformerRegistry.getTransformerFor(Practitioner.class, IMandator.class);
	}

	@Search
	public List<Practitioner> search(@OptionalParam(name = Practitioner.SP_NAME) StringParam name,
			@OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_FILTER) StringAndListParam theFtFilter) {
		IQuery<IMandator> query = coreModelService.getQuery(IMandator.class);

		if (name != null) {
			QueryUtil.andContactNameCriterion(query, name);
		}

		if (theFtFilter != null) {
			new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		}

		List<IMandator> practitioners = query.execute();
		List<Practitioner> _practitioners = contextService.submitContextInheriting(
				() -> practitioners.parallelStream().map(org -> getTransformer().getFhirObject(org))
						.filter(Optional::isPresent).map(Optional::get).toList());
		return _practitioners;
	}

}
