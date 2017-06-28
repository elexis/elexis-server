package info.elexis.server.findings.fhir.jpa.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.IdType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFamilyMemberHistory;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.util.ModelUtil;
import info.elexis.server.findings.fhir.jpa.model.annotated.AllergyIntolerance;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition_;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter_;
import info.elexis.server.findings.fhir.jpa.model.annotated.FamilyMemberHistory;
import info.elexis.server.findings.fhir.jpa.model.annotated.Observation;
import info.elexis.server.findings.fhir.jpa.model.annotated.Observation_;
import info.elexis.server.findings.fhir.jpa.model.annotated.ProcedureRequest;
import info.elexis.server.findings.fhir.jpa.model.annotated.ProcedureRequest_;
import info.elexis.server.findings.fhir.jpa.model.service.AbstractModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.AllergyIntoleranceModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.AllergyIntoleranceService;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionService;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterService;
import info.elexis.server.findings.fhir.jpa.model.service.FamilyMemberHistoryModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.FamilyMemberHistoryService;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery;
import info.elexis.server.findings.fhir.jpa.model.service.ObservationModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ObservationService;
import info.elexis.server.findings.fhir.jpa.model.service.ProcedureRequestModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ProcedureRequestService;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;
import info.elexis.server.findings.fhir.jpa.model.service.internal.InitializationRunner;

@Component
public class FindingsService implements IFindingsService {

	private Logger logger;

	private EncounterService encounterService;

	private ConditionService conditionService;

	private ProcedureRequestService procedureRequestService;

	private ObservationService observationService;
	
	private FamilyMemberHistoryService familyMemberHistoryService;

	private AllergyIntoleranceService allergyIntoleranceService;
	
	private InitializationRunner initializationRunner;

	public FindingsService() {
		encounterService = new EncounterService();
		conditionService = new ConditionService();
		procedureRequestService = new ProcedureRequestService();
		observationService = new ObservationService();
		familyMemberHistoryService = new FamilyMemberHistoryService();
		allergyIntoleranceService = new AllergyIntoleranceService();
	}

	@Activate
	protected void activate() {
		// make sure entities are available
		if (FindingsEntityManager.getEntityManager() == null) {
			LoggerFactory.getLogger(FindingsService.class).error("No findings entity manager available " + this);
		}
		LoggerFactory.getLogger(FindingsService.class).debug("New IFindingsService " + this);
		initializationRunner = new InitializationRunner(this);
		if (initializationRunner.shouldRun()) {
			initializationRunner.run();
		}
	}

	@Deactivate
	protected void deactivate() {
		if (initializationRunner != null) {
			initializationRunner.cancel();
		}
	}

	@Override
	public List<IFinding> getPatientsFindings(String patientId, Class<? extends IFinding> filter) {
		List<IFinding> ret = new ArrayList<>();
		if (patientId != null && !patientId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				ret.addAll(getEncounters(patientId));
			}
			if (filter.isAssignableFrom(ICondition.class)) {
				ret.addAll(getConditions(patientId, null));
			}
			if (filter.isAssignableFrom(IProcedureRequest.class)) {
				ret.addAll(getProcedureRequests(patientId, null));
			}
			// if (filter.isAssignableFrom(IClinicalImpression.class)) {
			// ret.addAll(getClinicalImpressions(patientId, null));
			// }
			if (filter.isAssignableFrom(IObservation.class)) {
				ret.addAll(getObservations(patientId, null));
			}
			if (filter.isAssignableFrom(IFamilyMemberHistory.class)) {
				ret.addAll(getFamilyMemberHistory(patientId));
			}
			if (filter.isAssignableFrom(IAllergyIntolerance.class)) {
				ret.addAll(getAllergyIntolerance(patientId));
			}
		}
		return ret;
	}

	@Override
	public List<IFinding> getConsultationsFindings(String consultationId, Class<? extends IFinding> filter) {
		List<IFinding> ret = new ArrayList<>();
		if (consultationId != null && !consultationId.isEmpty()) {
			Optional<IEncounter> encounter = getEncounter(consultationId);
			if (encounter.isPresent()) {
				if (filter.isAssignableFrom(IEncounter.class)) {
					ret.add(encounter.get());
				}
				if (filter.isAssignableFrom(ICondition.class)) {
					ret.addAll(getConditions(encounter.get().getPatientId(), encounter.get()));
				}
				if (filter.isAssignableFrom(IProcedureRequest.class)) {
					ret.addAll(getProcedureRequests(null, encounter.get().getId()));
				}
				// if (filter.isAssignableFrom(IClinicalImpression.class)) {
				// ret.addAll(getClinicalImpressions(patientId, null));
				// }
				if (filter.isAssignableFrom(IObservation.class)) {
					ret.addAll(getObservations(null, encounter.get().getId()));
				}

			}
		}
		return ret;
	}

	private List<ProcedureRequestModelAdapter> getProcedureRequests(String patientId, String encounterId) {
		JPAQuery<ProcedureRequest> query = new JPAQuery<>(ProcedureRequest.class);
		if (patientId != null) {
			query.add(ProcedureRequest_.patientid, JPAQuery.QUERY.EQUALS, patientId);
		}
		if (encounterId != null) {
			query.add(ProcedureRequest_.encounterid, JPAQuery.QUERY.EQUALS, encounterId);
		}
		List<ProcedureRequest> procedureRequests = query.execute();
		return procedureRequests.parallelStream().map(r -> new ProcedureRequestModelAdapter(r))
				.collect(Collectors.toList());
	}
	//
	// private List<ClinicalImpression> getClinicalImpressions(String patientId,
	// String encounterId) {
	// Query<ClinicalImpression> query = new Query<>(ClinicalImpression.class);
	// if (patientId != null) {
	// query.add(ClinicalImpression.FLD_PATIENTID, Query.EQUALS, patientId);
	// }
	// if (encounterId != null) {
	// query.add(ClinicalImpression.FLD_ENCOUNTERID, Query.EQUALS, encounterId);
	// }
	// return query.execute();
	// }

	@SuppressWarnings("unchecked")
	private List<ConditionModelAdapter> getConditions(String patientId, IEncounter encounter) {
		if (encounter != null) {
			List<ConditionModelAdapter> ret = new ArrayList<ConditionModelAdapter>();
			ret.addAll((Collection<? extends ConditionModelAdapter>) encounter.getIndication());
			return ret;
		}
		JPAQuery<Condition> query = new JPAQuery<>(Condition.class);
		if (patientId != null) {
			query.add(Condition_.patientid, JPAQuery.QUERY.EQUALS, patientId);
		}
		List<Condition> conditions = query.execute();
		return conditions.parallelStream().map(e -> new ConditionModelAdapter(e)).collect(Collectors.toList());
	}

	private List<ObservationModelAdapter> getObservations(String patientId, String encounterId) {
		JPAQuery<Observation> query = new JPAQuery<>(Observation.class);
		if (patientId != null) {
			query.add(Observation_.patientid, JPAQuery.QUERY.EQUALS, patientId);
		}
		if (encounterId != null) {
			query.add(Observation_.encounterid, JPAQuery.QUERY.EQUALS, encounterId);
		}
		List<Observation> observations = query.execute();
		return observations.parallelStream().map(e -> new ObservationModelAdapter(e)).collect(Collectors.toList());
	}

	private List<EncounterModelAdapter> getEncounters(String patientId) {
		if (patientId != null) {
			JPAQuery<Encounter> query = new JPAQuery<>(Encounter.class);
			query.add(Encounter_.patientid, JPAQuery.QUERY.EQUALS, patientId);
			List<Encounter> encounters = query.execute();
			return encounters.parallelStream().map(e -> new EncounterModelAdapter(e)).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private Optional<IEncounter> getEncounter(String consultationId) {
		JPAQuery<Encounter> query = new JPAQuery<>(Encounter.class);
		query.add(Encounter_.consultationid, JPAQuery.QUERY.EQUALS, consultationId);
		List<Encounter> encounters = query.execute();
		if (encounters != null && !encounters.isEmpty()) {
			if (encounters.size() > 1) {
				logger.warn("Too many encounters [" + encounters.size() + "] found for consultation [" + consultationId
						+ "] using first.");
			}
			return Optional.of(new EncounterModelAdapter(encounters.get(0)));
		}
		return Optional.empty();
	}
	
	private List<FamilyMemberHistoryModelAdapter> getFamilyMemberHistory(String patientId){
		JPAQuery<FamilyMemberHistory> query = new JPAQuery<>(FamilyMemberHistory.class);
		if (patientId != null) {
			query.add(Observation_.patientid, JPAQuery.QUERY.EQUALS, patientId);
		}
		List<FamilyMemberHistory> familyMemberHistories = query.execute();
		return familyMemberHistories.parallelStream()
			.map(e -> new FamilyMemberHistoryModelAdapter(e))
			.collect(Collectors.toList());
	}
	
	private List<AllergyIntoleranceModelAdapter> getAllergyIntolerance(String patientId){
		JPAQuery<AllergyIntolerance> query = new JPAQuery<>(AllergyIntolerance.class);
		if (patientId != null) {
			query.add(Observation_.patientid, JPAQuery.QUERY.EQUALS, patientId);
		}
		List<AllergyIntolerance> entries = query.execute();
		return entries.parallelStream()
			.map(e -> new AllergyIntoleranceModelAdapter(e)).collect(Collectors.toList());
	}

	@Override
	public void saveFinding(IFinding finding) {
		Object model = ((AbstractModelAdapter<?>) finding).getModel();
		if (model instanceof Encounter) {
			encounterService.write((Encounter) model);
			return;
		} else if (model instanceof Condition) {
			conditionService.write((Condition) model);
			return;
		} else if (model instanceof ProcedureRequest) {
			procedureRequestService.write((ProcedureRequest) model);
			return;
		} else if (model instanceof Observation) {
			observationService.write((Observation) model);
			return;
		} else if (model instanceof FamilyMemberHistory) {
			familyMemberHistoryService.write((FamilyMemberHistory) model);
			return;
		}
		else if (model instanceof AllergyIntolerance) {
			allergyIntoleranceService.write((AllergyIntolerance) model);
			return;
		}
		logger.error("Could not save unknown finding type [" + finding + "]");
	}

	@Override
	public void deleteFinding(IFinding finding) {
		Object model = ((AbstractModelAdapter<?>) finding).getModel();
		if (model instanceof Encounter) {
			encounterService.delete((Encounter) model);
			return;
		} else if (model instanceof Condition) {
			conditionService.delete((Condition) model);
			return;
		} else if (model instanceof ProcedureRequest) {
			procedureRequestService.delete((ProcedureRequest) model);
			return;
		} else if (model instanceof Observation) {
			observationService.delete((Observation) model);
			return;
		} else if (model instanceof FamilyMemberHistory) {
			familyMemberHistoryService.delete((FamilyMemberHistory) model);
			return;
		}
		else if (model instanceof AllergyIntolerance) {
			allergyIntoleranceService.delete((AllergyIntolerance) model);
			return;
		}
		logger.error("Could not delete unknown finding type [" + finding + "]");
	}

	//TODO refactor
	@Override
	public Optional<IFinding> findById(String id) {
		Optional<Encounter> encounter = encounterService.findById(id);
		if (encounter.isPresent()) {
			return Optional.of(new EncounterModelAdapter(encounter.get()));
		}
		Optional<Condition> condition = conditionService.findById(id);
		if (condition.isPresent()) {
			return Optional.of(new ConditionModelAdapter(condition.get()));
		}
		Optional<ProcedureRequest> procedureRequest = procedureRequestService.findById(id);
		if (procedureRequest.isPresent()) {
			return Optional.of(new ProcedureRequestModelAdapter(procedureRequest.get()));
		}
		Optional<Observation> observation = observationService.findById(id);
		if (observation.isPresent()) {
			return Optional.of(new ObservationModelAdapter(observation.get()));
		}
		Optional<FamilyMemberHistory> familyMemberHistory = familyMemberHistoryService.findById(id);
		if (familyMemberHistory.isPresent()) {
			return Optional.of(new FamilyMemberHistoryModelAdapter(familyMemberHistory.get()));
		}
		Optional<AllergyIntolerance> allergyIntolerance = allergyIntoleranceService.findById(id);
		if (allergyIntolerance.isPresent()) {
			return Optional.of(new AllergyIntoleranceModelAdapter(allergyIntolerance.get()));
		}
		return Optional.empty();
	}

	@Override
	public Optional<IFinding> findById(String id, Class<? extends IFinding> clazz) {
		if (clazz.isAssignableFrom(IEncounter.class)) {
			Optional<Encounter> encounter = encounterService.findById(id);
			if (encounter.isPresent()) {
				return Optional.of(new EncounterModelAdapter(encounter.get()));
			}
		}
		if (clazz.isAssignableFrom(ICondition.class)) {
			Optional<Condition> condition = conditionService.findById(id);
			if (condition.isPresent()) {
				return Optional.of(new ConditionModelAdapter(condition.get()));
			}
		}
		if (clazz.isAssignableFrom(IProcedureRequest.class)) {
			Optional<ProcedureRequest> procedureRequest = procedureRequestService.findById(id);
			if (procedureRequest.isPresent()) {
				return Optional.of(new ProcedureRequestModelAdapter(procedureRequest.get()));
			}
		}
		if (clazz.isAssignableFrom(IObservation.class)) {
			Optional<Observation> observation = observationService.findById(id);
			if (observation.isPresent()) {
				return Optional.of(new ObservationModelAdapter(observation.get()));
			}
		}
		if (clazz.isAssignableFrom(IFamilyMemberHistory.class)) {
			Optional<FamilyMemberHistory> familyMemberHistory =
				familyMemberHistoryService.findById(id);
			if (familyMemberHistory.isPresent()) {
				return Optional.of(new FamilyMemberHistoryModelAdapter(familyMemberHistory.get()));
			}
		}
		if (clazz.isAssignableFrom(IFamilyMemberHistory.class)) {
			Optional<AllergyIntolerance> allergyIntolerance =
				allergyIntoleranceService.findById(id);
			if (allergyIntolerance.isPresent()) {
				return Optional.of(new AllergyIntoleranceModelAdapter(allergyIntolerance.get()));
			}
		}
		
		return Optional.empty();
	}
	
	@Override
	public <T extends IFinding> T create(Class<T> type){
		if (type.equals(IEncounter.class)) {
			EncounterModelAdapter ret = new EncounterModelAdapter(encounterService.create());
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter =
				new org.hl7.fhir.dstu3.model.Encounter();
			fhirEncounter.setId(new IdType(fhirEncounter.getClass().getSimpleName(), ret.getId()));
			ModelUtil.saveResource(fhirEncounter, ret);
			saveFinding(ret);
			return type.cast(ret);
		} else if (type.equals(IObservation.class)) {
			ObservationModelAdapter ret = new ObservationModelAdapter(observationService.create());
			org.hl7.fhir.dstu3.model.Observation fhirObservation =
				new org.hl7.fhir.dstu3.model.Observation();
			fhirObservation
				.setId(new IdType(fhirObservation.getClass().getSimpleName(), ret.getId()));
			ModelUtil.saveResource(fhirObservation, ret);
			saveFinding(ret);
			return type.cast(ret);
		} else if (type.equals(ICondition.class)) {
			ConditionModelAdapter ret = new ConditionModelAdapter(conditionService.create());
			org.hl7.fhir.dstu3.model.Condition fhirCondition =
				new org.hl7.fhir.dstu3.model.Condition();
			fhirCondition.setId(new IdType(fhirCondition.getClass().getSimpleName(), ret.getId()));
			fhirCondition.setAssertedDate(new Date());
			ModelUtil.saveResource(fhirCondition, ret);
			saveFinding(ret);
			return type.cast(ret);
		} else if (type.equals(IClinicalImpression.class)) {
			//TODO ????
			return null;
		} else if (type.equals(IProcedureRequest.class)) {
			ProcedureRequestModelAdapter ret =
				new ProcedureRequestModelAdapter(procedureRequestService.create());
			org.hl7.fhir.dstu3.model.ProcedureRequest fhirProcedureRequest =
				new org.hl7.fhir.dstu3.model.ProcedureRequest();
			fhirProcedureRequest
				.setId(new IdType(fhirProcedureRequest.getClass().getSimpleName(), ret.getId()));
			ModelUtil.saveResource(fhirProcedureRequest, ret);
			saveFinding(ret);
			return type.cast(ret);
		} else if (type.equals(IFamilyMemberHistory.class)) {
			FamilyMemberHistoryModelAdapter ret =
				new FamilyMemberHistoryModelAdapter(familyMemberHistoryService.create());
			org.hl7.fhir.dstu3.model.FamilyMemberHistory fhirFamilyMemberHistory =
				new org.hl7.fhir.dstu3.model.FamilyMemberHistory();
			fhirFamilyMemberHistory
				.setId(new IdType(fhirFamilyMemberHistory.getClass().getSimpleName(), ret.getId()));
			ModelUtil.saveResource(fhirFamilyMemberHistory, ret);
			saveFinding(ret);
			return type.cast(ret);
		}
		else if (type.equals(IAllergyIntolerance.class)) {
			AllergyIntoleranceModelAdapter ret =
				new AllergyIntoleranceModelAdapter(allergyIntoleranceService.create());
			org.hl7.fhir.dstu3.model.AllergyIntolerance fhirAllergyIntolerance =
				new org.hl7.fhir.dstu3.model.AllergyIntolerance();
			fhirAllergyIntolerance
				.setId(new IdType(fhirAllergyIntolerance.getClass().getSimpleName(), ret.getId()));
			ModelUtil.saveResource(fhirAllergyIntolerance, ret);
			saveFinding(ret);
			return type.cast(ret);
		}
		return null;
	}
}
