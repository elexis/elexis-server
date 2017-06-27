package info.elexis.server.findings.fhir.jpa.model.service;

import javax.persistence.EntityManager;

import info.elexis.server.findings.fhir.jpa.model.annotated.FamilyMemberHistory;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;

public class FamilyMemberHistoryService extends AbstractService<FamilyMemberHistory> {
	
	public FamilyMemberHistoryService(){
		super(FamilyMemberHistory.class);
	}
	
	@Override
	protected EntityManager getEntityManager(){
		return FindingsEntityManager.getEntityManager();
	}
}
