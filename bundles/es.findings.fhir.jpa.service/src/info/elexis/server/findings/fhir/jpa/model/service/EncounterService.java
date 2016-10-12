package info.elexis.server.findings.fhir.jpa.model.service;

import javax.persistence.EntityManager;

import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;

public class EncounterService extends AbstractService<Encounter> {

	public EncounterService() {
		super(Encounter.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return FindingsEntityManager.getEntityManager();
	}
}
