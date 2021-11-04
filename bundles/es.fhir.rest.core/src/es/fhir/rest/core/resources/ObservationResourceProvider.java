package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.resources.util.CodeTypeUtil;
import es.fhir.rest.core.resources.util.DateRangeParamUtil;

@Component
public class ObservationResourceProvider implements IFhirResourceProvider {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Reference
	private IFindingsService findingsService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return Observation.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Observation, IObservation> getTransformer(){
		return (IFhirTransformer<Observation, IObservation>) transformerRegistry
			.getTransformerFor(Observation.class, IObservation.class);
	}
	
	@SuppressWarnings("unchecked")
	public IFhirTransformer<Observation, ILabResult> getLabTransformer(){
		return (IFhirTransformer<Observation, ILabResult>) transformerRegistry
			.getTransformerFor(Observation.class, ILabResult.class);
	}
	
	@Read
	public Observation getResourceById(@IdParam
	IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			// do lookup in findings first, then lab results
			Optional<IObservation> observation =
				findingsService.findById(idPart, IObservation.class);
			if (observation.isPresent()) {
				Optional<Observation> fhirObservation =
					getTransformer().getFhirObject(observation.get());
				return fhirObservation.get();
			}
			Optional<ILabResult> labresult = modelService.load(idPart, ILabResult.class);
			if (labresult.isPresent()) {
				Optional<Observation> fhirObservation =
					getLabTransformer().getFhirObject(labresult.get());
				return fhirObservation.get();
			}
		}
		return null;
	}
	
	@Search()
	public List<Observation> findObservation(@RequiredParam(name = Observation.SP_SUBJECT)
	IdType theSubjectId, @OptionalParam(name = Observation.SP_CATEGORY)
	CodeType categoryCode, @OptionalParam(name = Observation.SP_CODE)
	CodeType code, @OptionalParam(name = Observation.SP_DATE)
	DateRangeParam dates, @OptionalParam(name = Observation.SP_CONTEXT)
	IdType contextId){
		if (theSubjectId != null && !theSubjectId.isEmpty()) {
			Optional<IPatient> patient =
				modelService.load(theSubjectId.getIdPart(), IPatient.class);
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					List<Observation> ret = new ArrayList<Observation>();
					// laboratory
					if (categoryCode == null || ObservationCategory.LABORATORY.name()
						.equalsIgnoreCase(CodeTypeUtil.getCode(categoryCode).orElse(""))) {
						List<Observation> intermediateRet = new ArrayList<>();
						IQuery<ILabResult> resultQuery = modelService.getQuery(ILabResult.class);
						resultQuery.and(ModelPackage.Literals.ILAB_RESULT__PATIENT,
							COMPARATOR.EQUALS, patient.get());
						List<ILabResult> result = resultQuery.execute();
						
						result.parallelStream().forEach(r -> getLabTransformer().getFhirObject(r)
							.ifPresent(t -> intermediateRet.add(t)));
						ret = sortLaboratory(intermediateRet);
					}
					// all other observations
					List<IObservation> findings = findingsService
						.getPatientsFindings(theSubjectId.getIdPart(), IObservation.class);
					if (findings != null && !findings.isEmpty()) {
						for (IObservation iFinding : findings) {
							if (categoryCode != null
								&& !isObservationCategory(iFinding, categoryCode)) {
								continue;
							}
							Optional<Observation> fhirObservation =
								getTransformer().getFhirObject(iFinding);
							if (fhirObservation.isPresent()) {
								ret.add(fhirObservation.get());
							}
						}
					}
					if (dates != null) {
						ret = filterDates(ret, dates);
					}
					if (code != null) {
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
	
	private List<Observation> sortLaboratory(List<Observation> ret){
		Comparator<Observation> byGroup =
			(o1, o2) -> getElexisGroupCodingString(o1).compareTo(getElexisGroupCodingString(o2));
		
		return ret.stream().sorted(byGroup).collect(Collectors.toList());
	}
	
	private String getElexisGroupCodingString(Observation observation){
		List<Coding> codings = observation.getCode().getCoding();
		for (Coding coding : codings) {
			if (coding.getSystem().equals(CodingSystem.ELEXIS_LOCAL_LABORATORY_GROUP.getSystem())) {
				return coding.getCode();
			}
		}
		return "";
	}
	
	private List<Observation> filterCode(List<Observation> observations, CodeType code){
		ArrayList<Observation> ret = new ArrayList<>();
		String systemString = CodeTypeUtil.getSystem(code).orElse("");
		String codeString = CodeTypeUtil.getCode(code).orElse("");
		for (Observation observation : observations) {
			if (systemString.equals(CodingSystem.ELEXIS_LOCAL_LABORATORY_VITOLABKEY.getSystem())) {
				if (CodeTypeUtil.isVitoLabkey(modelService, observation, codeString)) {
					ret.add(observation);
				}
			} else if (CodeTypeUtil.isCodeInConcept(observation.getCode(), systemString,
				codeString)) {
				ret.add(observation);
			}
		}
		return ret;
	}
	
	private List<Observation> filterDates(List<Observation> observations, DateRangeParam dates){
		ArrayList<Observation> ret = new ArrayList<>();
		try {
			for (Observation observation : observations) {
				if (observation.hasEffectiveDateTimeType()) {
					if (DateRangeParamUtil.isDateInRange(observation.getEffectiveDateTimeType(),
						dates)) {
						ret.add(observation);
					}
				} else if (observation.hasEffectivePeriod()) {
					if (DateRangeParamUtil.isPeriodInRange(observation.getEffectivePeriod(),
						dates)) {
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
		if (idType.getValue() != null) {
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
	
	private boolean isObservationCategory(IObservation iObservation, CodeType observationCode){
		Optional<String> codeCode = CodeTypeUtil.getCode(observationCode);
		
		ObservationCategory category = iObservation.getCategory();
		if (category == null) {
			return false;
		}
		return category.name().equalsIgnoreCase(codeCode.orElse("").replaceAll("-", ""));
	}
	
	@Create
	public MethodOutcome createObservation(@ResourceParam
	Observation observation){
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
