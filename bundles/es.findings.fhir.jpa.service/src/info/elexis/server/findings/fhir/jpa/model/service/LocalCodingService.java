package info.elexis.server.findings.fhir.jpa.model.service;

import javax.persistence.EntityManager;

import info.elexis.server.findings.fhir.jpa.model.annotated.LocalCoding;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;

public class LocalCodingService extends AbstractService<LocalCoding> {

	public LocalCodingService() {
		super(LocalCoding.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return FindingsEntityManager.getEntityManager();
	}
}
