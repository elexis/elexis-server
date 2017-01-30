package info.elexis.server.core.connector.elexis.services;

import java.util.List;

import javax.persistence.EntityManager;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Diagnosis;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Diagnosis_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class DiagnosisService extends PersistenceService {

	private Diagnosis create(String code, String diagnosisClass, String text) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			em.getTransaction().begin();
			Diagnosis diagnosis = new Diagnosis();
			diagnosis.setCode(code);
			diagnosis.setText(text);
			diagnosis.setDiagnosisClass(diagnosisClass);
			em.getTransaction().commit();
			return diagnosis;
		} finally {
			em.close();
		}

	}

	public Diagnosis findExistingOrCreate(Diagnosis diag) {
		JPAQuery<Diagnosis> qre = new JPAQuery<>(Diagnosis.class);
		qre.add(Diagnosis_.code, QUERY.EQUALS, diag.getCode());
		qre.add(Diagnosis_.diagnosisClass, QUERY.EQUALS, diag.getDiagnosisClass());
		List<Diagnosis> results = qre.execute();
		if (results.size() > 0) {
			return results.get(0);
		}

		return create(diag.getCode(), diag.getDiagnosisClass(), diag.getText());
	}

}
