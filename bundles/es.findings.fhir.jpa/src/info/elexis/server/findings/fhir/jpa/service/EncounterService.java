package info.elexis.server.findings.fhir.jpa.service;

import javax.persistence.EntityManager;

import org.osgi.service.component.annotations.Component;

import info.elexis.server.findings.fhir.jpa.model.annotated.Encounter;
import info.elexis.server.findings.fhir.jpa.service.internal.FindingsEntityManager;

@Component
public class EncounterService extends AbstractService<Encounter> {

	public EncounterService() {
		super(Encounter.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return FindingsEntityManager.getEntityManager();
	}
}
