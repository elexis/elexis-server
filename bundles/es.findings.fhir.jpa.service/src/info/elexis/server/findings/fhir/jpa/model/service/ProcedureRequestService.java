package info.elexis.server.findings.fhir.jpa.model.service;

import javax.persistence.EntityManager;

import info.elexis.server.findings.fhir.jpa.model.annotated.ProcedureRequest;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;

public class ProcedureRequestService extends AbstractService<ProcedureRequest> {

	public ProcedureRequestService() {
		super(ProcedureRequest.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return FindingsEntityManager.getEntityManager();
	}
}
