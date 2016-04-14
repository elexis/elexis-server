package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Brief;

public class BriefService extends AbstractService<Brief> {

	public static BriefService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final BriefService INSTANCE = new BriefService();
	}

	private BriefService() {
		super(Brief.class);
	}

	@Override
	public Brief create() {
		Brief document = super.create();
		HeapService.INSTANCE.create(document.getId(), true);
		return document;
	}
}
