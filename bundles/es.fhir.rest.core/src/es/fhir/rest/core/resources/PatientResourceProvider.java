package es.fhir.rest.core.resources;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.Delete;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.rgw.tools.StringTool;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.resources.util.QueryUtil;

@Component
public class PatientResourceProvider implements IFhirResourceProvider {

	private Logger log;
	private ResourceProviderUtil resourceProviderUtil;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Patient.class;
	}

	@Activate
	public void activate() {
		log = LoggerFactory.getLogger(getClass());
		resourceProviderUtil = new ResourceProviderUtil();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Patient, IPatient> getTransformer() {
		return (IFhirTransformer<Patient, IPatient>) transformerRegistry.getTransformerFor(Patient.class,
				IPatient.class);
	}

	@Read
	public Patient getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IPatient> patient = modelService.load(idPart, IPatient.class);
			if (patient.isPresent() && patient.get().isPatient()) {
				Optional<Patient> fhirPatient = getTransformer().getFhirObject(patient.get());
				if (fhirPatient.isPresent()) {
					return fhirPatient.get();
				} else {
					// TODO
					log.error("");
				}
			}
		}
		return null;
	}

	@Search()
	public List<Patient> findPatientByIdentifier(@RequiredParam(name = Patient.SP_IDENTIFIER) IdentifierDt identifier) {
		if (identifier != null && identifier.getSystem().equals(IdentifierSystem.ELEXIS_PATNR.getSystem())) {
			INamedQuery<IPatient> namedQuery = modelService.getNamedQuery(IPatient.class, "code");
			Optional<IPatient> patient = namedQuery.executeWithParametersSingleResult(
					namedQuery.getParameterMap("code", StringTool.normalizeCase(identifier.getValue())));
			if (patient.isPresent() && patient.get().isPatient()) {
				Optional<Patient> fhirPatient = getTransformer().getFhirObject(patient.get());
				if(fhirPatient.isPresent()) {
					return Collections.singletonList(fhirPatient.get());
				}
			}
		}
		return Collections.emptyList();
	}

	/**
	 * 
	 * @param theNames     supports 0 .. 2 tokens. Additional tokens are not
	 *                     considered in the search. Multiple tokens are considered
	 *                     to reduce the amount of entries found, hence we alternate
	 *                     in filtering between {@link IPatient#getDescription1()}
	 *                     and {@link IPatient#getDescription2()}
	 * @param theBirthDate
	 * @param theSort
	 * @param theSummary
	 * @return
	 */
	@Search()
	public List<Patient> findPatient(@OptionalParam(name = Patient.SP_NAME) StringAndListParam theNames,
			@OptionalParam(name = Patient.SP_BIRTHDATE) DateParam theBirthDate, @Sort SortSpec theSort, SummaryEnum theSummary) {

		if (theNames == null && theBirthDate == null) {
			return Collections.emptyList();
		}

		IQuery<IPatient> query = modelService.getQuery(IPatient.class);
		List<String> nameParameters = null;

		if (theNames != null) {
			nameParameters = theNames.getValuesAsQueryTokens().stream()
					.flatMap(entry -> entry.getValuesAsQueryTokens().stream()).map(StringParam::getValue)
					.collect(Collectors.toList());
			if (!nameParameters.isEmpty()) {
				// use the first name parameter directly in the SQL query
				query.startGroup();
				query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE,
						"%" + nameParameters.get(0) + "%", true);
				query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE,
						"%" + nameParameters.get(0) + "%", true);
				query.andJoinGroups();
			}
		}

		if (theBirthDate != null) {
			LocalDate localDate = Instant.ofEpochMilli(theBirthDate.getValue().getTime()).atZone(ZoneId.systemDefault())
					.toLocalDate();
			query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS, localDate);
		}

		if (theSort != null) {
			String param = theSort.getParamName();
			SortOrderEnum order = theSort.getOrder();
			switch (param) {
			case Patient.SP_FAMILY:
				query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION1, QueryUtil.sortOrderEnumToLocal(order));
				break;
			case Patient.SP_GIVEN:
				query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION2, QueryUtil.sortOrderEnumToLocal(order));
				break;
			default:
				log.info("sortParamName [{}] not supported.", param);
				break;
			}
		}

		List<IPatient> patients = query.execute();
		if (!patients.isEmpty()) {
			if (nameParameters != null && nameParameters.size() > 1) {
				final String nameParam0 = nameParameters.get(0).toLowerCase();
				final String nameParam1 = nameParameters.get(1).toLowerCase();
				Predicate<IPatient> namePredicates = pat -> {
					String desc1 = (pat.getDescription1() != null) ? pat.getDescription1().toLowerCase() : "";
					String desc2 = (pat.getDescription2() != null) ? pat.getDescription2().toLowerCase() : "";
					if (desc1.contains(nameParam0)) {
						// if the first token was found in desc1, we have to switch places
						// otherwise we search the same thing again
						return !desc2.contains(nameParam1);
					} else {
						return !desc1.contains(nameParam1);
					}
				};
				patients.removeIf(namePredicates);
			}

			List<Patient> ret = patients.parallelStream().map(pat -> getTransformer().getFhirObject(pat, theSummary, Collections.emptySet()))
					.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
			return ret;
		}
		return Collections.emptyList();
	}

	@Create
	public MethodOutcome createPatient(@ResourceParam Patient patient) {
		return resourceProviderUtil.createResource(getTransformer(), patient, log);
	}

	@Update
	public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient patient) {
		// TODO request lock or fail
		return resourceProviderUtil.updateResource(theId, getTransformer(), patient, log);
	}

	@Delete
	public void deletePatient(@IdParam IdType theId) {
		// TODO request lock or fail
		if (theId != null) {
			Optional<IPatient> resource = modelService.load(theId.getIdPart(), IPatient.class);
			if (!resource.isPresent()) {
				throw new ResourceNotFoundException(theId);
			}
			modelService.delete(resource.get());
		}
	}

}
