package info.elexis.server.core.connector.elexis.services;

import static info.elexis.server.core.connector.elexis.internal.ElexisEntityManager.*;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;

import at.medevit.ch.artikelstamm.ArtikelstammHelper;
import ch.elexis.core.constants.StringConstants;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class ArtikelstammItemService extends AbstractService<ArtikelstammItem> {

	public static ArtikelstammItemService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final ArtikelstammItemService INSTANCE = new ArtikelstammItemService();
	}

	private ArtikelstammItemService() {
		super(ArtikelstammItem.class);
	}

	private static final String VERSION_ENTRY_ID = "VERSION";

	public int getCurrentVersion() {
		Optional<ArtikelstammItem> ai = findById(VERSION_ENTRY_ID);
		if (ai.isPresent()) {
			return Integer.parseInt(ai.get().getPpub());
		}
		return -1;
	}

	public void setCurrentVersion(Integer version) {
		Optional<ArtikelstammItem> ai = findById(VERSION_ENTRY_ID);
		if (ai.isPresent()) {
			ai.get().setPpub(Integer.toString(version));
		}
	}

	public void setImportSetCreationDate(Date creationDate) {
		Optional<ArtikelstammItem> version = findById(VERSION_ENTRY_ID);
		if (version.isPresent()) {
			DateFormat df = new SimpleDateFormat("ddMMyy HH:mm");
			version.get().setDscr(df.format(creationDate.getTime()));
		}

	}

	/**
	 * reset all black-box marks for the item to zero, we have to determine them
	 * fresh, otherwise once blackboxed - always blackboxed
	 * 
	 * @param importStammType
	 */
	public int resetAllBlackboxMarks() {
		EntityManager em = createEntityManager();
		try {
			CriteriaBuilder cb = em.getCriteriaBuilder();
			CriteriaUpdate<ArtikelstammItem> update = cb.createCriteriaUpdate(ArtikelstammItem.class);
			update.set(ArtikelstammItem_.bb, StringConstants.ZERO);
			return em.createQuery(update).executeUpdate();
		} finally {
			em.close();
		}
	}

	public ArtikelstammItem create(int cummulatedVersion, String gtin, BigInteger phar, String dscr) {
		em.getTransaction().begin();
		String id = ArtikelstammHelper.createUUID(cummulatedVersion, gtin, phar, true);
		String pharmacode = (phar != null) ? String.format("%07d", phar) : "0000000";
		ArtikelstammItem ai = create(id, false);
		ai.setCummVersion(Integer.toString(cummulatedVersion));
		// ai.setType(type.name());
		ai.setGtin(gtin);
		ai.setPhar(pharmacode);
		ai.setDscr(dscr);
		ai.setBb(StringConstants.ZERO);
		em.getTransaction().commit();
		return ai;
	}

	public boolean isLagerartikel(ArtikelstammItem ai) {
		if (ai.getIstbestand() > 0) {
			return true;
		}

		if (ai.getMinbestand() > 0 || ai.getMaxbestand() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * return all articles on stock, that is with a defined (>0) minbestand,
	 * maxbestand or istbestand
	 * 
	 * @return
	 */
	public static List<ArtikelstammItem> getAllStockArticles() {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.or(ArtikelstammItem_.minbestand, QUERY.GREATER, StringConstants.ZERO);
		qbe.or(ArtikelstammItem_.maxbestand, QUERY.GREATER, StringConstants.ZERO);
		return qbe.execute();
	}

	public static Optional<ArtikelstammItem> findByGTIN(String itemCode) {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.add(ArtikelstammItem_.gtin, QUERY.LIKE, itemCode);
		return qbe.executeGetSingleResult();
	}

	public static Optional<ArtikelstammItem> findByProductNumber(String code) {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.add(AbstractDBObjectIdDeleted_.id, QUERY.EQUALS, code);
		return qbe.executeGetSingleResult();
	}
}
