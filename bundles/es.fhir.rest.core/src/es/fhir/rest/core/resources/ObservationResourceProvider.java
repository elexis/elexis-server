package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.persistence.queries.ScrollableCursor;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.codes.CodingSystem;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.resources.util.CodeTypeUtil;
import es.fhir.rest.core.resources.util.DateRangeParamUtil;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult_;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.LabResultService;

@Component
public class ObservationResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Observation.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Observation, IObservation> getTransformer() {
		return (IFhirTransformer<Observation, IObservation>) transformerRegistry.getTransformerFor(Observation.class,
				IObservation.class);
	}

	@SuppressWarnings("unchecked")
	public IFhirTransformer<Observation, LabResult> getLabTransformer() {
		return (IFhirTransformer<Observation, LabResult>) transformerRegistry.getTransformerFor(Observation.class,
				LabResult.class);
	}

	@Read
	public Observation getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			// do lookup in findings first, then lab results
			Optional<IFinding> observation = findingsService.findById(idPart, IObservation.class);
			if (observation.isPresent() && (observation.get() instanceof IObservation)) {
				Optional<Observation> fhirObservation = getTransformer()
						.getFhirObject((IObservation) observation.get());
				return fhirObservation.get();
			}
			Optional<LabResult> labresult = LabResultService.load(idPart);
			if (labresult.isPresent()) {
				Optional<Observation> fhirObservation = getLabTransformer().getFhirObject(labresult.get());
				return fhirObservation.get();
			}
		}
		return null;
	}

	@Search()
	public List<Observation> findObservation(@RequiredParam(name = Observation.SP_SUBJECT) IdType theSubjectId,
			@OptionalParam(name = Observation.SP_CATEGORY) CodeType categoryCode,
			@OptionalParam(name = Observation.SP_CODE) CodeType code,
		@OptionalParam(name = Observation.SP_DATE) DateRangeParam dates,
		@OptionalParam(name = Observation.SP_CONTEXT) IdType contextId){
		if (theSubjectId != null && !theSubjectId.isEmpty()) {
			Optional<Kontakt> patient = KontaktService.load(theSubjectId.getIdPart());
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					List<Observation> ret = new ArrayList<Observation>();
					// laboratory
					if (categoryCode == null || ObservationCategory.LABORATORY.name()
							.equalsIgnoreCase(CodeTypeUtil.getCode(categoryCode).orElse(""))) {
						JPAQuery<LabResult> resultQuery = new JPAQuery<>(LabResult.class);
						resultQuery.add(LabResult_.patient, QUERY.EQUALS, patient.get());
						ScrollableCursor cursor = resultQuery.executeAsStream();
						while (cursor.hasNext()) {
							LabResult labResult = (LabResult) cursor.next();
							Optional<Observation> fhirObservation = getLabTransformer().getFhirObject(labResult);
							if (fhirObservation.isPresent()) {
								ret.add(fhirObservation.get());
							}
							cursor.clear();
						}
						cursor.close();
						ret = sortLaboratory(ret);
					}
					// all other observations
					List<IFinding> findings = findingsService.getPatientsFindings(theSubjectId.getIdPart(),
							IObservation.class);
					if (findings != null && !findings.isEmpty()) {
						for (IFinding iFinding : findings) {
							if (categoryCode != null && !isObservationCategory((IObservation) iFinding, categoryCode)) {
								continue;
							}
							Optional<Observation> fhirObservation = getTransformer()
									.getFhirObject((IObservation) iFinding);
							if (fhirObservation.isPresent()) {
								ret.add(fhirObservation.get());
							}
						}
					}
					if (dates != null) {
						ret = filterDates(ret, dates);
					}
					if(code != null) {
						ret = filterCode(ret, code);
					}
					if (contextId != null) {
						ret = filterContext(ret, contextId);
					}
					return ret;
				}
			}
		}
		return Collections.emptyList();
	}

	private List<Observation> sortLaboratory(List<Observation> ret) {
	    Comparator<Observation> byGroup = (o1, o2) -> 
	    	getElexisGroupCodingString(o1).compareTo(getElexisGroupCodingString(o2));

	    return ret.stream().sorted(byGroup).collect(Collectors.toList());
	}

	private String getElexisGroupCodingString(Observation observation) {
		List<Coding> codings = observation.getCode().getCoding();
		for (Coding coding : codings) {
			if (coding.getSystem().equals(CodingSystem.ELEXIS_LOCAL_LABORATORY_GROUP.getSystem())) {
				return coding.getCode();
			}
		}
		return "";
	}

	private List<Observation> filterCode(List<Observation> observations, CodeType code) {
		ArrayList<Observation> ret = new ArrayList<>();
		String systemString = CodeTypeUtil.getSystem(code).orElse("");
		String codeString = CodeTypeUtil.getCode(code).orElse("");
		for (Observation observation : observations) {
			if (systemString.equals(CodingSystem.ELEXIS_LOCAL_LABORATORY_VITOLABKEY.getSystem())) {
				if (CodeTypeUtil.isVitoLabkey(observation, codeString)) {
					ret.add(observation);
				}
			} else if (CodeTypeUtil.isCodeInConcept(observation.getCode(), systemString, codeString)) {
				ret.add(observation);
			}
		}
		return ret;
	}

	private List<Observation> filterDates(List<Observation> observations, DateRangeParam dates) {
		ArrayList<Observation> ret = new ArrayList<>();
		try {
			for (Observation observation : observations) {
				if (observation.hasEffectiveDateTimeType()) {
					if (DateRangeParamUtil.isDateInRange(observation.getEffectiveDateTimeType(), dates)) {
						ret.add(observation);
					}
				} else if (observation.hasEffectivePeriod()) {
					if (DateRangeParamUtil.isPeriodInRange(observation.getEffectivePeriod(), dates)) {
						ret.add(observation);
					}
				}
			}
		} catch (FHIRException fe) {
			return observations;
		}
		return ret;
	}
	
	private List<Observation> filterContext(List<Observation> observations, IdType idType){
		ArrayList<Observation> ret = new ArrayList<>();
		if (idType.getValue() != null)
		{
			for (Observation observation : observations) {
				if (observation.getContext() != null
					&& observation.getContext().hasReferenceElement() && idType.getValue()
						.equals(observation.getContext().getReferenceElement().getIdPart())) {
					ret.add(observation);
				}
			}
		}
		return ret;
	}
	
	private boolean isObservationCategory(IObservation iObservation, CodeType observationCode) {
		Optional<String> codeCode = CodeTypeUtil.getCode(observationCode);

		ObservationCategory category = iObservation.getCategory();
		if (category == null) {
			return false;
		}
		return category.name().equalsIgnoreCase(codeCode.orElse("").replaceAll("-", ""));
	}

	@Create
	public MethodOutcome createObservation(@ResourceParam Observation observation) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<IObservation> exists = getTransformer().getLocalObject(observation);
		if (exists.isPresent()) {
			outcome.setCreated(false);
			outcome.setId(new IdType(observation.getId()));
		} else {
			Optional<IObservation> created = getTransformer().createLocalObject(observation);
			if (created.isPresent()) {
				outcome.setCreated(true);
				outcome.setId(new IdType(created.get().getId()));
			} else {
				throw new InternalErrorException("Creation failed");
			}
		}
		return outcome;
	}
}
