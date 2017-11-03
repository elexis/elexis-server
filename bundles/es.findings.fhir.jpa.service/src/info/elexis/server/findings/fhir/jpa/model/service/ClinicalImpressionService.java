package info.elexis.server.findings.fhir.jpa.model.service;

import javax.persistence.EntityManager;

import info.elexis.server.findings.fhir.jpa.model.annotated.ClinicalImpression;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;

public class ClinicalImpressionService extends AbstractService<ClinicalImpression> {

	public ClinicalImpressionService() {
		super(ClinicalImpression.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return FindingsEntityManager.getEntityManager();
	}
}
