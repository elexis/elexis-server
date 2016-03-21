package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

public class LabResultService extends AbstractService<LabResult> {
	public static LabResultService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final LabResultService INSTANCE = new LabResultService();
	}

	private LabResultService() {
		super(LabResult.class);
	}

//	public LabResult create(Kontakt patContact, TimeTool date, LabItem labItm, String result, String comment,
//			Kontakt labContact) {
//		em.getTransaction().begin();
//		LabResult labResult = create(false);
//		labResult.setPatient(patContact);
//		labResult.setItem(labItm);
//		labResult.setResult(result);
//		labResult.setComment(comment);
//		labResult.setFlags(0);
//		em.getTransaction().commit();
//		return labResult;
//	}
}
