package info.elexis.server.findings.fhir.jpa.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.text.model.Samdas;
import ch.rgw.tools.VersionedResource;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.BehandlungService;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter_;
import info.elexis.server.findings.fhir.jpa.model.service.EncounterModelAdapter;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery;

@Component
public class MigratorService implements IMigratorService {

	private FindingsService findingsService;

	public MigratorService() {
		findingsService = new FindingsService();
		findingsService.activate();
	}

	@Override
	public void migratePatientsFindings(String patientId, Class<? extends IFinding> filter) {
		if (patientId != null && !patientId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				migratePatientEncounters(patientId);
			}
			if (filter.isAssignableFrom(ICondition.class)) {
				migratePatientCondition(patientId);
			}
		}
	}

	@Override
	public void migrateConsultationsFindings(String consultationId, Class<? extends IFinding> filter) {
		if (consultationId != null && !consultationId.isEmpty()) {
			if (filter.isAssignableFrom(IEncounter.class)) {
				migrateConsultationEncounter(consultationId);
			}
		}
	}

	/**
	 * Migrate the existing diagnose text of a patient to an {@link ICondition}
	 * instance. Migration is only performed if there is not already a diagnose
	 * in form of an {@link ICondition} present for the patient.
	 * 
	 * @param patientId
	 */
	private void migratePatientCondition(String patientId) {
		Optional<Kontakt> patient = KontaktService.INSTANCE.findById(patientId);
		patient.ifPresent(p -> {
			String diagnosis = p.getDiagnosen();
			if (diagnosis != null && !diagnosis.isEmpty()) {
				List<IFinding> conditions = findingsService.getPatientsFindings(patientId, ICondition.class);
				conditions = conditions.parallelStream()
						.filter(iFinding -> isDiagnose(iFinding))
						.collect(Collectors.toList());
				if (conditions.isEmpty()) {
					ICondition condition = findingsService.getFindingsFactory()
							.createCondition();
					condition.setPatientId(patientId);
					condition.setCategory(ConditionCategory.DIAGNOSIS);
					condition.setText(diagnosis);
					findingsService.saveFinding(condition);
				}
			}
		});
	}

	private boolean isDiagnose(IFinding iFinding) {
		return iFinding instanceof ICondition && ((ICondition) iFinding).getCategory() == ConditionCategory.DIAGNOSIS;
	}

	private void migratePatientEncounters(String patientId) {
		Optional<Kontakt> patient = KontaktService.INSTANCE.findById(patientId);
		patient.ifPresent(p -> {
			List<Behandlung> behandlungen = BehandlungService.findAllConsultationsForPatient(p);
			behandlungen.stream().forEach(b -> migrateEncounter(b));
		});
	}

	private void migrateConsultationEncounter(String consultationId) {
		Optional<Behandlung> behandlung = BehandlungService.INSTANCE.findById(consultationId);
		behandlung.ifPresent(b -> {
			migrateEncounter(b);
		});
	}

	private void migrateEncounter(Behandlung b) {
		String patientId = b.getFall().getPatientKontakt().getId();
		JPAQuery<Encounter> query = new JPAQuery<>(Encounter.class);
		query.add(Encounter_.patientid, JPAQuery.QUERY.EQUALS, patientId);
		query.add(Encounter_.consultationid, JPAQuery.QUERY.EQUALS, b.getId());
		List<Encounter> encounters = query.execute();
		if (encounters.isEmpty()) {
			createEncounter(b);
		} else {
			updateEncounter(new EncounterModelAdapter(encounters.get(0)), b);
		}
	}

	private void createEncounter(Behandlung behandlung) {
		IEncounter encounter = findingsService.getFindingsFactory().createEncounter();
		updateEncounter(encounter, behandlung);
	}

	private void updateEncounter(IEncounter encounter, Behandlung behandlung) {
		encounter.setConsultationId(behandlung.getId());
		encounter.setServiceProviderId(behandlung.getMandant().getId());

		LocalDate encounterDate = behandlung.getDatum();
		if (encounterDate != null) {
			encounter.setStartTime(encounterDate.atStartOfDay());
			encounter.setEndTime(encounterDate.atTime(23, 59, 59));
		}
		Fall fall = behandlung.getFall();
		if (fall != null) {
			Kontakt patient = fall.getPatientKontakt();
			if (patient != null) {
				encounter.setPatientId(patient.getId());
			}
		}

		VersionedResource vr = behandlung.getEintrag();
		if (vr != null) {
			Samdas samdas = new Samdas(vr.getHead());
			encounter.setText(samdas.getRecordText());
		}

		findingsService.saveFinding(encounter);
	}
}
