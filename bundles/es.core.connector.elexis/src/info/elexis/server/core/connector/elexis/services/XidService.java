package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Xid;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.XidQuality;

public class XidService extends AbstractService<Xid> {

	public static XidService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final XidService INSTANCE = new XidService();
	}

	private XidService() {
		super(Xid.class);
	}

	public void create(String domain, String domainId, AbstractDBObject obj, XidQuality quality, String type) {
		Xid xid = create();
		xid.setDomain(domain);
		xid.setDomainId(domainId);
		xid.setObject(obj.getId());
		xid.setQuality(quality);
		xid.setType(type);
	}

}
