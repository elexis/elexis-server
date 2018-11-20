//package info.elexis.server.core.connector.elexis.services;
//
//public class XidService extends PersistenceService {
//
////	private static Logger log = LoggerFactory.getLogger(XidService.class);
////
////	private static class Builder extends AbstractBuilder<Xid> {
////		public Builder(String domain, String domainId, AbstractDBObjectIdDeleted obj, XidQuality quality) {
////			object = new Xid();
////
////			object.setDomain(domain);
////			object.setDomainId(domainId);
////			object.setObject(obj.getId());
////			object.setQuality(quality);
////			object.setType(ElexisTypeMap.getKeyForObject(obj));
////		}
////	}
////
////	/**
////	 * Set or create an {@link Xid} for the provided values. If an existing
////	 * entry is found, the domainId value will be overwritten.
////	 * 
////	 * @param obj
////	 * @param domain
////	 * @param domainId
////	 * @param quality
////	 * @return <code>null</code> on error
////	 */
////	public static Xid setDomainId(AbstractDBObjectIdDeleted obj, String domain, String domainId, XidQuality quality) {
////		JPAQuery<Xid> qre = new JPAQuery<Xid>(Xid.class);
////		qre.add(Xid_.domain, JPAQuery.QUERY.LIKE, domain);
////		qre.add(Xid_.object, JPAQuery.QUERY.LIKE, obj.getId());
////		List<Xid> result = qre.execute();
////		if (result.size() == 0) {
////			return new Builder(domain, domainId, obj, quality).buildAndSave();
////		} else if (result.size() == 1) {
////			Xid xid = result.get(0);
////			xid.setDomainId(domainId);
////			xid = (Xid) XidService.save(xid);
////			return xid;
////		}
////		log.error("Multiple XID entries for {}, {}", domain, obj.getId());
////		return null;
////	}
////
////	/**
////	 * @param object
////	 * @param domain
////	 * @return the ID for a given object in the domain as stored by the
////	 *         {@link Xid} or <code>null</code>, if not found
////	 */
////	public static String getDomainId(AbstractDBObjectIdDeleted object, String domain) {
////		Optional<Xid> domainId = findByObjectAndDomain(object, domain);
////		if (domainId.isPresent()) {
////			return domainId.get().getDomainId();
////		}
////		return null;
////	}
////
////	public static void unsetDomainId(AbstractDBObjectIdDeleted object, String domain) {
////		Optional<Xid> domainId = findByObjectAndDomain(object, domain);
////		if (domainId.isPresent()) {
////			Xid xid = domainId.get();
////			delete(xid);
////		}
////	}
////
////	public static Optional<Xid> findByObjectAndDomain(AbstractDBObjectIdDeleted object, String domain) {
////		JPAQuery<Xid> qre = new JPAQuery<Xid>(Xid.class);
////		qre.add(Xid_.domain, JPAQuery.QUERY.LIKE, domain);
////		qre.add(Xid_.object, JPAQuery.QUERY.LIKE, object.getId());
////		// TODO add type as criteria?
////		List<Xid> results = qre.execute();
////
////		if (results.size() == 1) {
////			return Optional.of(results.get(0));
////		}
////		if (results.size() == 0) {
////			return Optional.empty();
////		}
////		log.warn("Multiple domainId entries for {} in domain {} found.", object.getId(), domain);
////		return Optional.empty();
////	}
//
//}
