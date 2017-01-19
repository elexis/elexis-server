package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import ch.elexis.core.model.IStockEntry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;

public class StockEntryService extends PersistenceService {

	public static class Builder extends AbstractBuilder<StockEntry> {
		public Builder(Stock stock, AbstractDBObjectIdDeleted article) {
			object = new StockEntry();
			object.setStock(stock);
			object.setArticle(article);
		}
		
		public Builder currentStock(int currentStock) {
			object.setCurrentStock(currentStock);
			return this;
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<StockEntry> load(String id) {
		return PersistenceService.load(StockEntry.class, id).map(v -> (StockEntry) v);
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
