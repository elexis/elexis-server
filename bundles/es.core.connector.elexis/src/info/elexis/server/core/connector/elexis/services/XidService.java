package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Xid;

public class XidService extends AbstractService<Xid> {

	public static XidService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final XidService INSTANCE = new XidService();
	}

	private XidService() {
		super(Xid.class);
	}

}
