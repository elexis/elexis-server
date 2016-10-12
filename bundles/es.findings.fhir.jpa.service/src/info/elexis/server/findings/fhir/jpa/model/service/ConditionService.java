package info.elexis.server.findings.fhir.jpa.model.service;

import javax.persistence.EntityManager;

import ch.elexis.core.findings.ICondition.ConditionCategory;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.findings.fhir.jpa.model.annotated.Condition;
import info.elexis.server.findings.fhir.jpa.model.service.internal.FindingsEntityManager;
import info.elexis.server.findings.fhir.jpa.service.FindingsFactory;

public class ConditionService extends AbstractService<Condition> {

	public ConditionService() {
		super(Condition.class);
	}

	@Override
	protected EntityManager getEntityManager() {
		return FindingsEntityManager.getEntityManager();
	}

	public ConditionModelAdapter createCondition(Kontakt patient) {
		FindingsFactory factory = new FindingsFactory();
		ConditionModelAdapter condition = (ConditionModelAdapter) factory.createCondition();
		return updateCondition(condition, patient);
	}

	public ConditionModelAdapter updateCondition(ConditionModelAdapter condition, Kontakt patient) {
		condition.setPatientId(patient.getId());

		condition.setCategory(ConditionCategory.DIAGNOSIS);

		write(condition.getModel());
		return condition;
	}
}
