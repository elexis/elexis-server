package info.elexis.server.core.connector.elexis.services;

import org.junit.BeforeClass;

import ch.elexis.core.model.IStock;
import ch.elexis.core.model.IStockEntry;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;

public class StockCommissioningSystemServiceTest {

	private static IStock testStockExisting;
	private static IStock testStockTarget;
	private static IStockEntry stockEntry_A, stockEntry_B;

	private static ArtikelstammItem ai, item2;

	private static StockService stockService = new StockService();

	@BeforeClass
	public static void setUp() throws Exception {
		testStockExisting = new StockService.Builder("TSE", 15).buildAndSave();

		ai = ArtikelstammItemService.load(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID).get();
		stockEntry_A = stockService.storeArticleInStock(testStockExisting, StoreToStringService.storeToString(ai));
		stockEntry_A.setMinimumStock(7);
		stockEntry_A.setCurrentStock(12);
		stockEntry_A.setMaximumStock(14);
		StockEntryService.save((StockEntry) stockEntry_A);

		item2 = ArtikelstammItemService.load(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID_2).get();
		stockEntry_B = stockService.storeArticleInStock(testStockExisting, StoreToStringService.storeToString(item2));
		stockEntry_B.setMinimumStock(5);
		stockEntry_B.setCurrentStock(15);
		stockEntry_B.setMaximumStock(20);

		testStockTarget = new StockService.Builder("TST", 15).buildAndSave();

		stockEntry_B = stockService.storeArticleInStock(testStockTarget, StoreToStringService.storeToString(item2));
		stockEntry_B.setMinimumStock(5);
		stockEntry_B.setCurrentStock(10);
		stockEntry_B.setMaximumStock(15);
		StockEntryService.save((StockEntry) stockEntry_B);
	}

	// @Test
	// public void testSynchronizeInventory() {
	// StockCommissioningSystemService.INSTANCE.synchronizeInventory(testStockTarget,
	// null, null);
	// List<StockEntry> shouldEqualTargetNow =
	// stockService.findAllStockEntriesForStock(testStockExisting);
	// assertEquals(1, shouldEqualTargetNow.size());
	// assertEquals(item2.getId(), shouldEqualTargetNow.get(0).getArticleId());
	// assertEquals(10, shouldEqualTargetNow.get(0).getCurrentStock());
	// }

}
