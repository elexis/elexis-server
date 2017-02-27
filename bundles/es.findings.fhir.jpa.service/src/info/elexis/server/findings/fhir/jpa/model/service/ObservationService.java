package info.elexis.server.findings.fhir.jpa.model.service;

import javax.persistence.EntityManager;

import info.elexis.server.findings.fhir.jpa.model.annotated.Observation;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;

public class ObservationService extends AbstractService<Observation> {

	public ObservationService() {
		super(Observation.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return FindingsEntityManager.getEntityManager();
	}
}
