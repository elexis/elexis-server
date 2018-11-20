//package info.elexis.server.core.connector.elexis.services;
//
//import static info.elexis.server.core.connector.elexis.internal.ElexisEntityManager.createEntityManager;
//
//import java.math.BigInteger;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Optional;
//
//import javax.persistence.EntityManager;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaUpdate;
//
//import at.medevit.ch.artikelstamm.ArtikelstammHelper;
//import ch.elexis.core.constants.StringConstants;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem_;
//import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
//
//public class ArtikelstammItemService extends PersistenceService {
//
//	private static final String VERSION_ENTRY_ID = "VERSION";
//
//	public static class Builder extends AbstractBuilder<ArtikelstammItem> {
//		public Builder(int cummulatedVersion, String gtin, BigInteger phar, String dscr) {
//			object = new ArtikelstammItem();
//			String id = ArtikelstammHelper.createUUID(cummulatedVersion, gtin, phar, true);
//			object.setId(id);
//			String pharmacode = (phar != null) ? String.format("%07d", phar) : "0000000";
//			object.setPhar(pharmacode);
//			object.setCummVersion(Integer.toString(cummulatedVersion));
//			object.setGtin(gtin);
//			object.setDscr(dscr);
//			object.setBb(StringConstants.ZERO);
//		}
//	}
//
//	/**
//	 * convenience method
//	 * 
//	 * @param id
//	 * @return
//	 */
//	public static Optional<ArtikelstammItem> load(String id) {
//		return PersistenceService.load(ArtikelstammItem.class, id).map(v -> (ArtikelstammItem) v);
//	}
//
//	public static int getCurrentVersion() {
//		Optional<ArtikelstammItem> ai = load(VERSION_ENTRY_ID);
//		if (ai.isPresent()) {
//			return Integer.parseInt(ai.get().getPpub());
//		}
//		return -1;
//	}
//
//	public static void setCurrentVersion(Integer version) {
//		Optional<ArtikelstammItem> ai = load(VERSION_ENTRY_ID);
//		if (ai.isPresent()) {
//			ai.get().setPpub(Integer.toString(version));
//		}
//	}
//
//	public static void setImportSetCreationDate(Date creationDate) {
//		Optional<ArtikelstammItem> version = load(VERSION_ENTRY_ID);
//		if (version.isPresent()) {
//			DateFormat df = new SimpleDateFormat("ddMMyy HH:mm");
//			version.get().setDscr(df.format(creationDate.getTime()));
//		}
//	}
//
//	/**
//	 * reset all black-box marks for the item to zero, we have to determine them
//	 * fresh, otherwise once blackboxed - always blackboxed
//	 * 
//	 * @param importStammType
//	 */
//	public static int resetAllBlackboxMarks() {
//		EntityManager em = createEntityManager();
//		try {
//			CriteriaBuilder cb = em.getCriteriaBuilder();
//			CriteriaUpdate<ArtikelstammItem> update = cb.createCriteriaUpdate(ArtikelstammItem.class);
//			update.set(ArtikelstammItem_.bb, StringConstants.ZERO);
//			return em.createQuery(update).executeUpdate();
//		} finally {
//			em.close();
//		}
//	}
//
//	/**
//	 * Finds an {@link ArtikelstammItem} by its GTIN. Does not consider
//	 * black-boxed articles.
//	 * 
//	 * @param itemCode
//	 * @return
//	 */
//	public static Optional<ArtikelstammItem> findByGTIN(String itemCode) {
//		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
//		qbe.add(ArtikelstammItem_.gtin, QUERY.LIKE, itemCode);
//		qbe.add(ArtikelstammItem_.bb, QUERY.EQUALS, 0);
//		return qbe.executeGetSingleResult();
//	}
//
//	public static Optional<ArtikelstammItem> findByProductNumber(String code) {
//		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
//		qbe.add(AbstractDBObjectIdDeleted_.id, QUERY.EQUALS, code);
//		return qbe.executeGetSingleResult();
//	}
//}
