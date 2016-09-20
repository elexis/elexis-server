package info.elexis.server.findings.fhir.jpa.service;

import java.time.LocalDate;

import javax.persistence.EntityManager;

import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.service.internal.FindingsEntityManager;

@Component(service = EncounterService.class)
public class EncounterService extends AbstractService<Encounter> {

	public EncounterService() {
		super(Encounter.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return FindingsEntityManager.getEntityManager();
	}

	public Encounter createEncounter(Behandlung behandlung) {
		FindingsFactory factory = new FindingsFactory();
		Encounter encounter = (Encounter) factory.createEncounter();
		updateEncounter(encounter, behandlung);
		return encounter;
	}

	public Encounter updateEncounter(Encounter encounter, Behandlung behandlung) {
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
		return write(encounter);
	}
}
