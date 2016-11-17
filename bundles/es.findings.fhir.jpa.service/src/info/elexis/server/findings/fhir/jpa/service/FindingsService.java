package info.elexis.server.findings.fhir.jpa.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsFactory;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IProcedureRequest;
import info.elexis.server.core.connector.elexis.common.DBConnection;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition_;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter_;
import info.elexis.server.findings.fhir.jpa.model.annotated.ProcedureRequest;
import info.elexis.server.findings.fhir.jpa.model.annotated.ProcedureRequest_;
import info.elexis.server.findings.fhir.jpa.model.service.AbstractModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ConditionService;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterService;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery;
import info.elexis.server.findings.fhir.jpa.model.service.ProcedureRequestModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.ProcedureRequestService;
import info.elexis.server.findings.fhir.jpa.model.service.internal.DbInitializer;

@Component
public class FindingsService implements IFindingsService {

	private Logger logger;

	private static boolean dbInitialized;

	private static ReentrantLock dbInitializedLock = new ReentrantLock();

	private FindingsFactory factory;

	private EncounterService encounterService;

	private ConditionService conditionService;

	private ProcedureRequestService procedureRequestService;

	@Activate
	protected void activate() {
		getLogger().debug("New IFindingsService " + this);
		try {
			dbInitializedLock.lock();
			if (!dbInitialized) {
				Optional<DBConnection> connectionOpt = ElexisDBConnection.getConnection();
				if (connectionOpt.isPresent()) {
					DbInitializer initializer = new DbInitializer(connectionOpt.get());
					initializer.init();
				}
			}
		} catch (Exception e) {
			getLogger().debug("Error activating IFindingsService " + this, e);
		} finally {
			dbInitializedLock.unlock();
		}
		factory = new FindingsFactory();
		encounterService = new EncounterService();
		conditionService = new ConditionService();
		procedureRequestService = new ProcedureRequestService();
	}

	private Logger getLogger() {
		if (logger == null) {
			logger = LoggerFactory.getLogger(FindingsService.class);
		}
		return logger;
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
			// if (filter.isAssignableFrom(IObservation.class)) {
			// ret.addAll(getObservations(patientId, null));
			// }
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
				// if (filter.isAssignableFrom(IObservation.class)) {
				// ret.addAll(getObservations(patientId, null));
				// }

			}
		}
		return ret;
	}

	private List<ProcedureRequestModelAdapter> getProcedureRequests(String patientId, String encounterId) {
		List<ProcedureRequestModelAdapter> ret = new ArrayList<>();
		JPAQuery<ProcedureRequest> query = new JPAQuery<>(ProcedureRequest.class);
		if (patientId != null) {
			query.add(ProcedureRequest_.patientid, JPAQuery.QUERY.EQUALS, patientId);
		}
		if (encounterId != null) {
			query.add(ProcedureRequest_.encounterid, JPAQuery.QUERY.EQUALS, encounterId);
		}
		List<ProcedureRequest> procedureRequests = query.execute();
		procedureRequests.parallelStream().forEach(r -> ret.add(new ProcedureRequestModelAdapter(r)));
		return ret;
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
		List<ConditionModelAdapter> ret = new ArrayList<>();
		JPAQuery<Condition> query = new JPAQuery<>(Condition.class);
		if (patientId != null) {
			query.add(Condition_.patientid, JPAQuery.QUERY.EQUALS, patientId);
		}
		List<Condition> conditions = query.execute();
		conditions.parallelStream().forEach(e -> ret.add(new ConditionModelAdapter(e)));
		return ret;
	}

	// private List<Observation> getObservations(String patientId, String
	// encounterId) {
	// Query<Observation> query = new Query<>(Observation.class);
	// if (patientId != null) {
	// query.add(Observation.FLD_PATIENTID, Query.EQUALS, patientId);
	// }
	// if (encounterId != null) {
	// query.add(Observation.FLD_ENCOUNTERID, Query.EQUALS, encounterId);
	// }
	// return query.execute();
	// }

	private List<EncounterModelAdapter> getEncounters(String patientId) {
		List<EncounterModelAdapter> ret = new ArrayList<>();
		if (patientId != null) {
			JPAQuery<Encounter> query = new JPAQuery<>(Encounter.class);
			query.add(Encounter_.patientid, JPAQuery.QUERY.EQUALS, patientId);
			List<Encounter> encounters = query.execute();
			encounters.parallelStream().forEach(e -> ret.add(new EncounterModelAdapter(e)));
		}
		return ret;
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

	@Override
	public void saveFinding(IFinding finding) {
		factory.saveFinding((AbstractModelAdapter<?>) finding);
	}

	@Override
	public void deleteFinding(IFinding finding) {
		factory.deleteFinding((AbstractModelAdapter<?>) finding);
	}

	@Override
	public IFindingsFactory getFindingsFactory() {
		return factory;
	}

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
		return Optional.empty();
	}
}
