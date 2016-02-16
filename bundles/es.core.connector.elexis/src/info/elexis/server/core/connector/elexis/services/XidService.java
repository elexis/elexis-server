package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
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

	public Xid create(String domain, String domainId, AbstractDBObjectIdDeleted obj, XidQuality quality) {
		em.getTransaction().begin();
		Xid xid = create(false);
		xid.setDomain(domain);
		xid.setDomainId(domainId);
		xid.setObject(obj.getId());
		xid.setQuality(quality);
		xid.setType(ElexisTypeMap.getKeyForObject(obj));
		em.getTransaction().commit();
		return xid;
	}

}
