package info.elexis.server.findings.fhir.jpa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsFactory;
import ch.elexis.core.findings.IFindingsService;
import info.elexis.server.core.connector.elexis.common.DBConnection;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter_;
import info.elexis.server.findings.fhir.jpa.service.internal.DbInitializer;

@Component
public class FindingsService implements IFindingsService {

	private static Logger logger = LoggerFactory.getLogger(FindingsService.class);

	private static boolean dbInitialized;

	private static ReentrantLock dbInitializedLock = new ReentrantLock();

	private FindingsFactory factory;

	public FindingsService() {
		try {
			dbInitializedLock.lock();
			if(!dbInitialized) {
				Optional<DBConnection> connectionOpt = ElexisDBConnection.getConnection();
				if (connectionOpt.isPresent()) {
					DbInitializer initializer = new DbInitializer(connectionOpt.get());
					initializer.init();
				}
			}
		} finally {
			dbInitializedLock.unlock();
		}
		factory = new FindingsFactory();
	}

	@Override
	public List<IFinding> getPatientsFindings(String patientId, Class<? extends IFinding> filter) {
		List<IFinding> ret = new ArrayList<>();
		if (patientId != null && !patientId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				ret.addAll(getEncounters(patientId));
			}
			// if (filter.isAssignableFrom(ICondition.class)) {
			// ret.addAll(getConditions(patientId, null));
			// }
			// if (filter.isAssignableFrom(IClinicalImpression.class)) {
			// ret.addAll(getClinicalImpressions(patientId, null));
			// }
			// if (filter.isAssignableFrom(IObservation.class)) {
			// ret.addAll(getObservations(patientId, null));
			// }
			// if (filter.isAssignableFrom(IProcedureRequest.class)) {
			// ret.addAll(getProcedureRequests(patientId, null));
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
			// if (filter.isAssignableFrom(ICondition.class)) {
			// ret.addAll(getConditions(patientId, null));
			// }
			// if (filter.isAssignableFrom(IClinicalImpression.class)) {
			// ret.addAll(getClinicalImpressions(patientId, null));
			// }
			// if (filter.isAssignableFrom(IObservation.class)) {
			// ret.addAll(getObservations(patientId, null));
			// }
			// if (filter.isAssignableFrom(IProcedureRequest.class)) {
			// ret.addAll(getProcedureRequests(patientId, null));
			// }
			}
		}
		return ret;
	}

	// private List<ProcedureRequest> getProcedureRequests(String patientId,
	// String encounterId) {
	// Query<ProcedureRequest> query = new Query<>(ProcedureRequest.class);
	// if (patientId != null) {
	// query.add(ProcedureRequest.FLD_PATIENTID, Query.EQUALS, patientId);
	// }
	// if (encounterId != null) {
	// query.add(ProcedureRequest.FLD_ENCOUNTERID, Query.EQUALS, encounterId);
	// }
	// return query.execute();
	// }
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
	//
	// private List<Condition> getConditions(String patientId, String
	// encounterId) {
	// Query<Condition> query = new Query<>(Condition.class);
	// if (patientId != null) {
	// query.add(Condition.FLD_PATIENTID, Query.EQUALS, patientId);
	// }
	// if (encounterId != null) {
	// query.add(Condition.FLD_ENCOUNTERID, Query.EQUALS, encounterId);
	// }
	// return query.execute();
	// }
	//
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

	private List<Encounter> getEncounters(String patientId) {
		JPAQuery<Encounter> query = new JPAQuery<>(Encounter.class);
		if (patientId != null) {
			query.add(Encounter_.patientid, QUERY.EQUALS, patientId);
		}
		return query.execute();
	}

	private Optional<IEncounter> getEncounter(String consultationId) {
		JPAQuery<Encounter> query = new JPAQuery<>(Encounter.class);
		query.add(Encounter_.consultationid, QUERY.EQUALS, consultationId);
		List<Encounter> encounters = query.execute();
		if (encounters != null && !encounters.isEmpty()) {
			if (encounters.size() > 1) {
				logger.warn("Too many encounters [" + encounters.size() + "] found for consultation [" + consultationId
						+ "] using first.");
			}
			return Optional.of(encounters.get(0));
		}
		return Optional.empty();
	}

	@Override
	public void saveFinding(IFinding finding) {
		factory.saveFinding(finding);
	}

	@Override
	public void deleteFinding(IFinding finding) {
		factory.deleteFinding(finding);
	}

	@Override
	public IFindingsFactory getFindingsFactory() {
		return factory;
	}

}
