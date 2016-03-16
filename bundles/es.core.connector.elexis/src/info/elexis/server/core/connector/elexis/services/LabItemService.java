package info.elexis.server.core.connector.elexis.services;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.types.LabItemTyp;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;

public class LabItemService extends AbstractService<LabItem> {

	public static LabItemService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final LabItemService INSTANCE = new LabItemService();
	}

	private LabItemService() {
		super(LabItem.class);
	}

	public ILabItem create(String code, String title, IContact laboratory, String refMale, String refFemale,
			String unit, LabItemTyp type, String group, int seq) {
		em.getTransaction().begin();
		LabItem labItem = create(false);
		labItem.setCode(code);
		labItem.setName(title);
		labItem.setLabor(KontaktService.INSTANCE.findById(laboratory.getId()));
		labItem.setReferenceMale(refMale);
		labItem.setReferenceFemale(refFemale);
		labItem.setUnit(unit);
		labItem.setTyp(type);
		labItem.setGroup(group);
		labItem.setPriority(Integer.toString(seq));
		em.getTransaction().commit();
		return labItem;
	}

}
