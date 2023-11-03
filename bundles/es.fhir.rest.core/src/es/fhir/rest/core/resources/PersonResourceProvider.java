package es.fhir.rest.core.resources;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Person;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import es.fhir.rest.core.resources.util.IContactSearchFilterQueryAdapter;
import es.fhir.rest.core.resources.util.QueryUtil;

@Component(service = IFhirResourceProvider.class)
public class PersonResourceProvider extends AbstractFhirCrudResourceProvider<Person, IPerson> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private ILocalLockService localLockService;

	@Reference
	private IContextService contextService;

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	public PersonResourceProvider() {
		super(IPerson.class);
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Person.class;
	}

	@Activate
	public void activate() {
		super.setModelService(coreModelService);
		super.setLocalLockService(localLockService);
	}

	@Override
	public IFhirTransformer<Person, IPerson> getTransformer() {
		return transformerRegistry.getTransformerFor(Person.class, IPerson.class);
	}

	@Search
	public List<Person> search(@OptionalParam(name = "_id") StringAndListParam theId,
			@OptionalParam(name = Person.SP_IDENTIFIER) TokenParam identifier,
			@OptionalParam(name = Person.SP_NAME) StringParam theName,
			@OptionalParam(name = Person.SP_BIRTHDATE) DateParam theBirthDate,
			@OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_FILTER) StringAndListParam theFtFilter,
			@Sort SortSpec theSort, SummaryEnum theSummary) {

		IQuery<IPerson> query = coreModelService.getQuery(IPerson.class);

		if (theId != null) {
			List<StringOrListParam> id_values = theId.getValuesAsQueryTokens();
			for (StringOrListParam id_value : id_values) {
				query.or("id", COMPARATOR.EQUALS, id_value.getValuesAsQueryTokens().get(0).getValue());
			}
		}

		if (theName != null) {
			QueryUtil.andContactNameCriterion(query, theName);
		}

		if (theBirthDate != null) {
			LocalDate localDate = Instant.ofEpochMilli(theBirthDate.getValue().getTime()).atZone(ZoneId.systemDefault())
					.toLocalDate();
			query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS, localDate);
		}

		if (theFtFilter != null) {
			new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		}

		List<IPerson> persons = query.execute();
		List<Person> _persons = contextService.submitContextInheriting(() -> persons.parallelStream()
				.map(org -> getTransformer().getFhirObject(org, theSummary, Collections.emptySet()))
				.filter(Optional::isPresent).map(Optional::get).toList());
		return _persons;
	}

}
