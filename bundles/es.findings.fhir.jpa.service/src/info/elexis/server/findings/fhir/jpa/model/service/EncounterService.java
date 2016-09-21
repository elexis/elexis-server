package info.elexis.server.findings.fhir.jpa.model.service;

import java.time.LocalDate;

import javax.persistence.EntityManager;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;
import info.elexis.server.findings.fhir.jpa.service.FindingsFactory;

public class EncounterService extends AbstractService<Encounter> {

	public EncounterService() {
		super(Encounter.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return FindingsEntityManager.getEntityManager();
	}

	public EncounterModelAdapter createEncounter(Behandlung behandlung) {
		FindingsFactory factory = new FindingsFactory();
		EncounterModelAdapter encounter = (EncounterModelAdapter) factory.createEncounter();
		return updateEncounter(encounter, behandlung);
	}

	public EncounterModelAdapter updateEncounter(EncounterModelAdapter encounter, Behandlung behandlung) {
		encounter.setConsultationId(behandlung.getId());
		LocalDate encounterDate = behandlung.getDatum();
		if (encounterDate != null) {
			encounter.setEffectiveTime(encounterDate.atStartOfDay());
		}
		Fall fall = behandlung.getFall();
		if (fall != null) {
			Kontakt patient = fall.getPatientKontakt();
			if (patient != null) {
				encounter.setPatientId(patient.getId());
			}
		}
		write(encounter.getModel());
		return encounter;
	}
}
