//package info.elexis.server.core.connector.elexis.services;
//
//import ch.elexis.core.model.IStockEntry;
//
//public class StockEntryService extends PersistenceService {
//
//	/**
//	 * Is this stock entry bound for reordering? This is the case if either a
//	 * minimum or maximum stock amount is defined (i.e. > 0).
//	 * 
//	 * @param se
//	 * @return
//	 */
//	public static boolean isStockEntryBoundForReorder(IStockEntry se) {
//		return se.getMinimumStock() > 0 || se.getMaximumStock() > 0;
//	}
//
//}
