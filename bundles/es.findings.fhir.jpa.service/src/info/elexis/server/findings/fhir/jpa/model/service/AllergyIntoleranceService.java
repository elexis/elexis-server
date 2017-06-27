package info.elexis.server.findings.fhir.jpa.model.service;

import javax.persistence.EntityManager;

import info.elexis.server.findings.fhir.jpa.model.annotated.AllergyIntolerance;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;

public class AllergyIntoleranceService extends AbstractService<AllergyIntolerance> {
	
	public AllergyIntoleranceService(){
		super(AllergyIntolerance.class);
	}
	
	@Override
	protected EntityManager getEntityManager(){
		return FindingsEntityManager.getEntityManager();
	}
}
