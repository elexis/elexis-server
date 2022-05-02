package es.fhir.rest.core.resources;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import es.fhir.rest.core.resources.util.IContactSearchFilterQueryAdapter;
import es.fhir.rest.core.resources.util.QueryUtil;

@Component(service = IFhirResourceProvider.class)
public class PatientResourceProvider extends AbstractFhirCrudResourceProvider<Patient, IPatient> {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	public PatientResourceProvider(){
		super(IPatient.class);
	}
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Patient.class;
	}
	
	@Activate
	public void activate(){
		super.setCoreModelService(coreModelService);
	}
	
	@Override
	public IFhirTransformer<Patient, IPatient> getTransformer(){
		return (IFhirTransformer<Patient, IPatient>) transformerRegistry
			.getTransformerFor(Patient.class, IPatient.class);
	}
	
	@Search
	public List<Patient> search(@OptionalParam(name="_id") StringAndListParam theId, 
		@OptionalParam(name = Patient.SP_IDENTIFIER) TokenParam identifier,
		@OptionalParam(name = Patient.SP_NAME) StringParam theName,
		@OptionalParam(name = Patient.SP_BIRTHDATE) DateParam theBirthDate,
		@OptionalParam(name = Patient.SP_ACTIVE) StringParam isActive,
		@OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_FILTER) StringAndListParam theFtFilter,
		@Sort SortSpec theSort, SummaryEnum theSummary){
		
		boolean includeDeleted = (isActive != null) ? Boolean.valueOf(isActive.getValue()) : false;
		IQuery<IPatient> query = coreModelService.getQuery(IPatient.class, includeDeleted);
		
		if(theId != null) {
			List<StringOrListParam> id_values = theId.getValuesAsQueryTokens();
			for (StringOrListParam id_value : id_values) {
				query.and("id", COMPARATOR.EQUALS, id_value.getValuesAsQueryTokens().get(0).getValue());
			}
		}
		
		if (identifier != null
			&& Objects.equals(IdentifierSystem.ELEXIS_PATNR.getSystem(), identifier.getSystem())) {
			query.and(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.EQUALS,
				identifier.getValue());
		}
		
		if (theName != null) {
			QueryUtil.andContactNameCriterion(query, theName);
		}
		
		if (theBirthDate != null) {
			LocalDate localDate = Instant.ofEpochMilli(theBirthDate.getValue().getTime())
				.atZone(ZoneId.systemDefault()).toLocalDate();
			query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS, localDate);
		}
		
		if (theFtFilter != null) {
			new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
		}
		
		if (theSort != null) {
			String param = theSort.getParamName();
			SortOrderEnum order = theSort.getOrder();
			switch (param) {
			case Patient.SP_FAMILY:
				query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION1,
					QueryUtil.sortOrderEnumToLocal(order));
				break;
			case Patient.SP_GIVEN:
				query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION2,
					QueryUtil.sortOrderEnumToLocal(order));
				break;
			default:
				log.info("sortParamName [{}] not supported.", param);
				break;
			}
		}
		
		List<IPatient> patients = query.execute();
		List<Patient> _patients = patients.parallelStream()
			.map(org -> getTransformer().getFhirObject(org, theSummary, Collections.emptySet()))
			.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
		return _patients;
	}
	
}
