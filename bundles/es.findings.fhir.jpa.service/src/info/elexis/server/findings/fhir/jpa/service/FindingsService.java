package info.elexis.server.findings.fhir.jpa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsFactory;
import ch.elexis.core.findings.IFindingsService;
import info.elexis.server.core.connector.elexis.common.DBConnection;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter_;
import info.elexis.server.findings.fhir.jpa.model.service.AbstractModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterService;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery;
import info.elexis.server.findings.fhir.jpa.model.service.internal.DbInitializer;

@Component
public class FindingsService implements IFindingsService {

	private Logger logger;

	private static boolean dbInitialized;

	private static ReentrantLock dbInitializedLock = new ReentrantLock();

	private static ReentrantLock createOrUpdateLock = new ReentrantLock();

	private FindingsFactory factory;

	private EncounterService encounterService;

	private boolean createOrUpdateFindings;

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

	private List<EncounterModelAdapter> getEncounters(String patientId) {
		List<EncounterModelAdapter> ret = new ArrayList<>();
		if (patientId != null) {
			if (createOrUpdateFindings) {
				createOrUpdateLock.lock();
				try {
					List<Behandlung> behandlungen = getBehandlungen(patientId);
					for (Behandlung behandlung : behandlungen) {
						JPAQuery<Encounter> query = new JPAQuery<>(Encounter.class);
						query.add(Encounter_.patientid, JPAQuery.QUERY.EQUALS, patientId);
						query.add(Encounter_.consultationid, JPAQuery.QUERY.EQUALS, behandlung.getId());
						List<Encounter> encounters = query.execute();
						if (encounters.isEmpty()) {
							ret.add(encounterService.createEncounter(behandlung));
						} else {
							ret.add(encounterService.updateEncounter(new EncounterModelAdapter(encounters.get(0)),
									behandlung));
						}
					}
				} finally {
					createOrUpdateLock.unlock();
				}
			} else {
				JPAQuery<Encounter> query = new JPAQuery<>(Encounter.class);
				query.add(Encounter_.patientid, JPAQuery.QUERY.EQUALS, patientId);
				List<Encounter> encounters = query.execute();
				encounters.stream().forEach(e -> ret.add(new EncounterModelAdapter(e)));
			}
		}
		return ret;
	}

	private List<Behandlung> getBehandlungen(String patientId) {
		List<Behandlung> ret = new ArrayList<>();
		Optional<Kontakt> patient = KontaktService.INSTANCE.findById(patientId);
		if (patient.isPresent()) {
			info.elexis.server.core.connector.elexis.services.JPAQuery<Fall> queryFall = new info.elexis.server.core.connector.elexis.services.JPAQuery<>(
					Fall.class);
			queryFall.add(Fall_.patientKontakt, info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY.EQUALS,
					patient.get());
			List<Fall> faelle = queryFall.execute();
			for (Fall fall : faelle) {
				info.elexis.server.core.connector.elexis.services.JPAQuery<Behandlung> queryBehandlung = new info.elexis.server.core.connector.elexis.services.JPAQuery<>(
						Behandlung.class);
				queryBehandlung.add(Behandlung_.fall,
						info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY.EQUALS, fall);
				List<Behandlung> behandlungen = queryBehandlung.execute();
				ret.addAll(behandlungen);
			}
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
		return Optional.empty();
	}

	@Override
	public void setCreateOrUpdate(boolean value) {
		createOrUpdateFindings = value;
	}

	@Override
	public boolean getCreateOrUpdate() {
		return createOrUpdateFindings;
	}

}
