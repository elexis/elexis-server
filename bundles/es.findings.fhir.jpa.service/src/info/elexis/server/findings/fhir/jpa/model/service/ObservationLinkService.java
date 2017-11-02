package info.elexis.server.findings.fhir.jpa.model.service;

import javax.persistence.EntityManager;

import info.elexis.server.findings.fhir.jpa.model.annotated.ObservationLink;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;

public class ObservationLinkService extends AbstractService<ObservationLink> {

	public ObservationLinkService() {
		super(ObservationLink.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return FindingsEntityManager.getEntityManager();
	}
}
