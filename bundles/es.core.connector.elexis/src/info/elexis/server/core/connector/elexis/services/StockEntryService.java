package info.elexis.server.core.connector.elexis.services;

import ch.elexis.core.model.IStockEntry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;

public class StockEntryService extends AbstractService<StockEntry> {

	public static StockEntryService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final StockEntryService INSTANCE = new StockEntryService();
	}

	private StockEntryService() {
		super(StockEntry.class);
	}

	public IStockEntry create(Stock stock, AbstractDBObjectIdDeleted article, Integer currentStock) {
		em.getTransaction().begin();
		StockEntry stockEntry = create(false);
		stockEntry.setStock(stock);
		stockEntry.setArticle(article);
		if (currentStock != null) {
			stockEntry.setCurrentStock(currentStock.intValue());
		}
		em.getTransaction().commit();

		// refresh Stock#getEntities
		StockService.INSTANCE.refresh(stock);

		return stockEntry;
	}

	/**
	 * Is this stock entry bound for reordering? This is the case if either a
	 * minimum or maximum stock amount is defined (i.e. > 0).
	 * 
	 * @param se
	 * @return
	 */
	public static boolean isStockEntryBoundForReorder(IStockEntry se) {
		return se.getMinimumStock() > 0 || se.getMaximumStock() > 0;
	}

}
