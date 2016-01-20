package info.elexis.server.core.connector.elexis.services;

import static info.elexis.server.core.connector.elexis.internal.ElexisEntityManager.em;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import at.medevit.ch.artikelstamm.ArtikelstammConstants.TYPE;
import at.medevit.ch.artikelstamm.ArtikelstammHelper;
import ch.elexis.core.constants.StringConstants;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.meta.ArtikelstammItem_;

public class ArtikelstammItemService extends AbstractService<ArtikelstammItem> {

	public static ArtikelstammItemService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final ArtikelstammItemService INSTANCE = new ArtikelstammItemService();
	}

	private ArtikelstammItemService() {
		super(ArtikelstammItem.class);
	}

	private static final String VERSION_ENTRY_ID = "VERSION";

	/**
	 * @param stammType
	 * @return The version of the resp {@link TYPE}, or 99999 if not found or
	 *         error
	 */
	public int getImportSetCumulatedVersion(TYPE stammType) {
		ArtikelstammItem ai = findById(VERSION_ENTRY_ID);
		switch (stammType) {
		case N:
			return Integer.parseInt(ai.getPexf());
		case P:
			return Integer.parseInt(ai.getPpub());
		}
		return 99999;
	}

	public void setImportSetCumulatedVersion(TYPE importStammType, Integer version) {
		ArtikelstammItem ai = findById(VERSION_ENTRY_ID);
		switch (importStammType) {
		case N:
			ai.setPexf(Integer.toString(version));
			return;
		case P:
			ai.setPpub(Integer.toString(version));
			return;
		}
	}

	public static void setImportSetDataQuality(TYPE importStammType, Integer dataquality) {
		// TODO Auto-generated method stub

	}

	public static void setImportSetCreationDate(TYPE importStammType, Date time) {
		// TODO Auto-generated method stub

	}

	/**
	 * return all articles on stock, that is with a defined (>0) minbestand, maxbestand or istbestand
	 * @return
	 */
	public static List<ArtikelstammItem> getAllStockArticles() {
		JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
		qbe.or(ArtikelstammItem_.minbestand, JPAQuery.QUERY.GREATER, StringConstants.ZERO);
		qbe.or(ArtikelstammItem_.maxbestand, JPAQuery.QUERY.GREATER, StringConstants.ZERO);
		return qbe.execute();
	}

	/**
	 * reset all black-box marks for the item to zero, we have to determine them
	 * fresh, otherwise once blackboxed - always blackboxed
	 * 
	 * @param importStammType
	 */
	public int resetAllBlackboxMarks(TYPE importStammType) {
		CriteriaBuilder cb = em().getCriteriaBuilder();
		CriteriaUpdate<ArtikelstammItem> update = cb.createCriteriaUpdate(ArtikelstammItem.class);
		Root<ArtikelstammItem> e = update.from(ArtikelstammItem.class);
		update.set(ArtikelstammItem_.bb, StringConstants.ZERO);
		update.where(cb.equal(e.get(ArtikelstammItem_.type), importStammType.name()));
		return em().createQuery(update).executeUpdate();
	}

	public ArtikelstammItem create(int cummulatedVersion, TYPE type, String gtin, BigInteger phar, String dscr, String addscr) {
		em().getTransaction().begin();
		String id = ArtikelstammHelper.createUUID(cummulatedVersion, type, gtin, phar);
		String pharmacode = (phar != null) ? String.format("%07d", phar) : "0000000";
		ArtikelstammItem ai = create(id, false);
		ai.setCummVersion(Integer.toString(cummulatedVersion));
		ai.setType(type.name());
		ai.setGtin(gtin);
		ai.setPhar(pharmacode);
		ai.setDscr(dscr);
		ai.setAdddscr(addscr);
		ai.setBb(StringConstants.ZERO);
		em().getTransaction().commit();
		return ai;
	}
}
