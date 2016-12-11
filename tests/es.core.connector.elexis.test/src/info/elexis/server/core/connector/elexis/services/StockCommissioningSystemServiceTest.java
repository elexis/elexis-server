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

	@BeforeClass
	public static void setUp() throws Exception {
		testStockExisting = StockService.INSTANCE.create("TSE", 15);

		ai = ArtikelstammItemService.INSTANCE.findById(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID).get();
		stockEntry_A = StockService.INSTANCE.storeArticleInStock(testStockExisting,
				StoreToStringService.storeToString(ai));
		stockEntry_A.setMinimumStock(7);
		stockEntry_A.setCurrentStock(12);
		stockEntry_A.setMaximumStock(14);
		StockEntryService.INSTANCE.write((StockEntry) stockEntry_A);

		item2 = ArtikelstammItemService.INSTANCE.findById(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID_2).get();
		stockEntry_B = StockService.INSTANCE.storeArticleInStock(testStockExisting,
				StoreToStringService.storeToString(item2));
		stockEntry_B.setMinimumStock(5);
		stockEntry_B.setCurrentStock(15);
		stockEntry_B.setMaximumStock(20);
		
		testStockTarget = StockService.INSTANCE.create("TST", 15);

		stockEntry_B = StockService.INSTANCE.storeArticleInStock(testStockTarget,
				StoreToStringService.storeToString(item2));
		stockEntry_B.setMinimumStock(5);
		stockEntry_B.setCurrentStock(10);
		stockEntry_B.setMaximumStock(15);
		StockEntryService.INSTANCE.write((StockEntry) stockEntry_B);
	}

//	@Test
//	public void testSynchronizeInventory() {
//		StockCommissioningSystemService.INSTANCE.synchronizeInventory(testStockTarget, null, null);
//		List<StockEntry> shouldEqualTargetNow = StockService.INSTANCE.findAllStockEntriesForStock(testStockExisting);
//		assertEquals(1, shouldEqualTargetNow.size());
//		assertEquals(item2.getId(), shouldEqualTargetNow.get(0).getArticleId());
//		assertEquals(10, shouldEqualTargetNow.get(0).getCurrentStock());
//	}

}
