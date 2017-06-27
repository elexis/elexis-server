package info.elexis.server.findings.fhir.jpa.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.core.text.model.Samdas;
import ch.rgw.tools.VersionedResource;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.BehandlungService;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter_;
import info.elexis.server.findings.fhir.jpa.model.service.JPAQuery;

@Component
public class MigratorService implements IMigratorService {

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@Override
	public void migratePatientsFindings(String patientId, Class<? extends IFinding> filter,
		ICoding iCoding){
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
		Optional<Kontakt> patient = KontaktService.load(patientId);
		patient.ifPresent(p -> {
			String diagnosis = p.getDiagnosen();
			if (diagnosis != null && !diagnosis.isEmpty()) {
				List<IFinding> conditions = findingsService.getPatientsFindings(patientId, ICondition.class);
				conditions = conditions.stream()
						.filter(iFinding -> isDiagnose(iFinding))
						.collect(Collectors.toList());
				if (conditions.isEmpty()) {
					ICondition condition = findingsService.create(ICondition.class);
					condition.setPatientId(patientId);
					condition.setCategory(ConditionCategory.PROBLEMLISTITEM);
					condition.setText(diagnosis);
					findingsService.saveFinding(condition);
				}
			}
		});
	}

	private boolean isDiagnose(IFinding iFinding) {
		return iFinding instanceof ICondition
				&& ((ICondition) iFinding).getCategory() == ConditionCategory.PROBLEMLISTITEM;
	}

	private void migratePatientEncounters(String patientId) {
		Optional<Kontakt> patient = KontaktService.load(patientId);
		patient.ifPresent(p -> {
			List<Behandlung> behandlungen = BehandlungService.findAllConsultationsForPatient(p);
			behandlungen.stream().forEach(b -> migrateEncounter(b));
		});
	}

	private void migrateConsultationEncounter(String consultationId) {
		Optional<Behandlung> behandlung = BehandlungService.load(consultationId);
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
		}
	}

	private void createEncounter(Behandlung behandlung) {
		IEncounter encounter = findingsService.create(IEncounter.class);
		updateEncounter(encounter, behandlung);
	}

	private void updateEncounter(IEncounter encounter, Behandlung behandlung) {
		encounter.setConsultationId(behandlung.getId());
		encounter.setMandatorId(behandlung.getMandant().getId());

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

		List<ICoding> coding = encounter.getType();
		if (!ModelUtil.isSystemInList(CodingSystem.ELEXIS_ENCOUNTER_TYPE.getSystem(), coding)) {
			coding.add(
					new TransientCoding(CodingSystem.ELEXIS_ENCOUNTER_TYPE.getSystem(), "text",
					"Nicht strukturierte Konsultation"));
			encounter.setType(coding);
		}

		findingsService.saveFinding(encounter);
	}
}
