//package info.elexis.server.core.connector.elexis.services;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.List;
//
//import org.eclipse.core.runtime.IStatus;
//import org.junit.After;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import ch.elexis.core.constants.Preferences;
//import ch.elexis.core.model.IStockEntry;
//import ch.elexis.core.services.IStockService.Availability;
//import info.elexis.server.core.connector.elexis.AllTestsSuite;
//import info.elexis.server.core.connector.elexis.billable.VerrechenbarArtikelstammItem;
//import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
//import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;
//
//public class StockServiceTest extends AbstractServiceTest {
//
//	private static Stock defaultStock;
//	private static Stock rowaStock;
//
//	private static StockService stockService = new StockService();
//	private static ArtikelstammItem article_A;
//	private static ArtikelstammItem item2, item3;
//	private static String artikel_AStS;
//	private static String artikel_item2, artikel_item3;
//
//	@BeforeClass
//	public static void init() throws IOException, SQLException {
//		AllTestsSuite.getInitializer().initializeArtikelstamm();
//
//		article_A = ArtikelstammItemService.load(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID).get();
//		artikel_AStS = StoreToStringService.storeToString(article_A);
//
//		item2 = ArtikelstammItemService.load(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID_2).get();
//		artikel_item2 = StoreToStringService.storeToString(item2);
//
//		item3 = ArtikelstammItemService.load(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID_3).get();
//		artikel_item3 = StoreToStringService.storeToString(item3);
//
//		defaultStock = StockService.load("STD").get();
//
//		rowaStock = StockService.load(AllTestsSuite.RWA_ID).get();
//
//		IStockEntry stockEntry_A = stockService.storeArticleInStock(defaultStock, artikel_AStS);
//		stockEntry_A.setMinimumStock(5);
//		stockEntry_A.setCurrentStock(10);
//		stockEntry_A.setMaximumStock(15);
//		StockEntryService.save((StockEntry) stockEntry_A);
//	}
//
//	@After
//	public void teardownPatientAndBehandlung() {
//		cleanup();
//	}
//
//	@Test
//	public void testStoreUnstoreFindPreferredArticleInStock() {
//		List<? extends IStockEntry> a_entries = stockService.findAllStockEntriesForArticle(artikel_AStS);
//		assertEquals(1, a_entries.size());
//	}
//
//	@Test
//	public void testPerformDisposalAndReturnOfArticle() {
//		ArtikelstammItem artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264").get();
//
//		Stock stock = new StockService.Builder("TMP", 20).buildAndSave();
//		IStockEntry se = stockService.storeArticleInStock(stock, StoreToStringService.storeToString(artikelstammItem));
//		se.setMinimumStock(15);
//		se.setCurrentStock(13);
//		StockEntryService.save((StockEntry) se);
//
//		stockService.performSingleDisposal(artikelstammItem, 5, null);
//		IStockEntry prefSE = stockService
//				.findPreferredStockEntryForArticle(StoreToStringService.storeToString(artikelstammItem), null);
//
//		assertEquals(((StockEntry) se).getId(), ((StockEntry) prefSE).getId());
//		assertEquals(8, prefSE.getCurrentStock());
//		assertEquals(15, prefSE.getMinimumStock());
//
//		stockService.performSingleReturn(artikelstammItem, 3, null);
//		prefSE = stockService.findPreferredStockEntryForArticle(StoreToStringService.storeToString(artikelstammItem),
//				null);
//		assertEquals(((StockEntry) se).getId(), ((StockEntry) prefSE).getId());
//		assertEquals(11, prefSE.getCurrentStock());
//		assertEquals(15, prefSE.getMinimumStock());
//
//		List<StockEntry> entries = StockService.load(stock.getId()).get().getEntries();
//		assertEquals(1, entries.size());
//
//		StockService.remove(StockService.load(stock.getId()).get());
//	}
//
//	@Test
//	public void testGetCummulatedCountForArticle() {
//		Stock stock = new StockService.Builder("TMP", 20).buildAndSave();
//		IStockEntry stockEntry_B = stockService.storeArticleInStock(stock, artikel_AStS);
//		stockEntry_B.setMinimumStock(5);
//		stockEntry_B.setCurrentStock(10);
//		stockEntry_B.setMaximumStock(15);
//		StockEntryService.save((StockEntry) stockEntry_B);
//
//		IStockEntry stockEntry_C = stockService.storeArticleInStock(stock, artikel_item2);
//		stockEntry_C.setMinimumStock(15);
//		stockEntry_C.setCurrentStock(10);
//		stockEntry_C.setMaximumStock(20);
//		StockEntryService.save((StockEntry) stockEntry_C);
//
//		IStockEntry stockEntry_D = stockService.storeArticleInStock(stock, artikel_item3);
//		stockEntry_D.setMinimumStock(5);
//		stockEntry_D.setCurrentStock(5);
//		stockEntry_D.setMaximumStock(20);
//		StockEntryService.save((StockEntry) stockEntry_D);
//
//		IStockEntry stockEntry_Dd = stockService.storeArticleInStock(defaultStock, artikel_item3);
//		stockEntry_Dd.setMinimumStock(15);
//		stockEntry_Dd.setCurrentStock(0);
//		stockEntry_Dd.setMaximumStock(20);
//		StockEntryService.save((StockEntry) stockEntry_Dd);
//
//		Integer nullValue = stockService.getCumulatedStockForArticle(null);
//		assertNull(nullValue);
//
//		Integer cumulatedStockForArticle = stockService.getCumulatedStockForArticle(article_A);
//		assertEquals(20, cumulatedStockForArticle.intValue());
//
//		Availability availability = stockService.getCumulatedAvailabilityForArticle(article_A);
//		assertEquals(Availability.IN_STOCK, availability);
//
//		Availability availability3 = stockService.getCumulatedAvailabilityForArticle(item2);
//		assertEquals(Availability.CRITICAL_STOCK, availability3);
//
//		Availability availability4 = stockService.getCumulatedAvailabilityForArticle(item2);
//		assertEquals(Availability.CRITICAL_STOCK, availability4);
//	}
//
//	@Test
//	public void testOutlayOnBillingArticleIncluding6324() {
//		ArtikelstammItem artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264").get();
//		IStockEntry se = stockService.storeArticleInStock(rowaStock,
//				StoreToStringService.storeToString(artikelstammItem));
//		se.setMinimumStock(500);
//		se.setCurrentStock(800);
//		StockEntryService.save((StockEntry) se);
//
//		ArtikelstammItem someItem = ArtikelstammItemService.load("0403265107202630212210008").get();
//		IStockEntry se2 = stockService.storeArticleInStock(rowaStock, StoreToStringService.storeToString(someItem));
//		se2.setMinimumStock(50);
//		se2.setCurrentStock(80);
//		StockEntryService.save((StockEntry) se2);
//
//		createTestMandantPatientFallBehandlung();
//
//		VerrechenbarArtikelstammItem verrechenbar = new VerrechenbarArtikelstammItem(artikelstammItem);
//		VerrechenbarArtikelstammItem pharmaV = new VerrechenbarArtikelstammItem(someItem);
//
//		// do not outlay
//		IStatus status = verrechenbar.add(testBehandlungen.get(0), testContacts.get(0), null, 0.5f);
//		assertTrue(status.isOK());
//		assertEquals(800, stockService
//				.findStockEntryForArticleInStock(rowaStock, StoreToStringService.storeToString(artikelstammItem))
//				.getCurrentStock());
//
//		status = verrechenbar.add(testBehandlungen.get(0), testContacts.get(0), null, 5);
//		assertTrue(status.isOK());
//		assertEquals(795, stockService
//				.findStockEntryForArticleInStock(rowaStock, StoreToStringService.storeToString(artikelstammItem))
//				.getCurrentStock());
//		status = verrechenbar.add(testBehandlungen.get(0), testContacts.get(0), null, 2);
//		assertTrue(status.isOK());
//		assertEquals(793, stockService
//				.findStockEntryForArticleInStock(rowaStock, StoreToStringService.storeToString(artikelstammItem))
//				.getCurrentStock());
//
//		// do outlay
//		ConfigService.INSTANCE.set(Preferences.INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES, Boolean.TRUE.toString());
//		status = pharmaV.add(testBehandlungen.get(0), testContacts.get(0), null, 0.5f);
//		assertTrue(status.isOK());
//		assertEquals(79,
//				stockService.findStockEntryForArticleInStock(rowaStock, StoreToStringService.storeToString(someItem))
//						.getCurrentStock());
//
//		stockService.unstoreArticleFromStock(rowaStock, StoreToStringService.storeToString(someItem));
//		stockService.unstoreArticleFromStock(rowaStock, StoreToStringService.storeToString(artikelstammItem));
//	}
//
//	@Test
//	public void testModifyStock() {
//		// article with 16 pieces per package
//		ArtikelstammItem artikelstammItem = ArtikelstammItemService.load("0768065339003664487240008").get();
//		IStockEntry stockEntry_A = stockService.storeArticleInStock(defaultStock,
//				StoreToStringService.storeToString(artikelstammItem));
//		stockEntry_A.setMinimumStock(5);
//		stockEntry_A.setCurrentStock(10);
//		stockEntry_A.setMaximumStock(15);
//		StockEntryService.save((StockEntry) stockEntry_A);
//
//		// test with article that has a defined package size
//		stockService.modifyStockCount(stockEntry_A, -1);
//		assertStockEntryEquals(stockEntry_A, 9, 0);
//		stockService.modifyStockCount(stockEntry_A, -0.5f);
//		assertStockEntryEquals(stockEntry_A, 8, 8);
//		stockService.modifyStockCount(stockEntry_A, -0.5f);
//		assertStockEntryEquals(stockEntry_A, 8, 0);
//		stockService.modifyStockCount(stockEntry_A, -1.5f);
//		assertStockEntryEquals(stockEntry_A, 6, 8);
//		stockService.modifyStockCount(stockEntry_A, -1.5f);
//		assertStockEntryEquals(stockEntry_A, 5, 0);
//		stockService.modifyStockCount(stockEntry_A, 0.5f);
//		assertStockEntryEquals(stockEntry_A, 5, 8);
//		stockService.modifyStockCount(stockEntry_A, 0f);
//		assertStockEntryEquals(stockEntry_A, 5, 8);
//		stockService.modifyStockCount(stockEntry_A, -1);
//		assertStockEntryEquals(stockEntry_A, 4, 8);
//		stockService.modifyStockCount(stockEntry_A, 1);
//		assertStockEntryEquals(stockEntry_A, 5, 8);
//
//		// test with article without a defined package size
//		artikelstammItem.setPkg_size(0);
//		ArtikelstammItemService.save(artikelstammItem);
//		stockService.modifyStockCount(stockEntry_A, -1);
//		assertStockEntryEquals(stockEntry_A, 4, 8);
//		stockService.modifyStockCount(stockEntry_A, -0.5f);
//		assertStockEntryEquals(stockEntry_A, 3, 8);
//
//		artikelstammItem.setPkg_size(16);
//		ArtikelstammItemService.save(artikelstammItem);
//	}
//
//	private void assertStockEntryEquals(IStockEntry se, int currentStock, int fractionUnits) {
//		assertEquals(currentStock, se.getCurrentStock());
//		assertEquals(fractionUnits, se.getFractionUnits());
//	}
//
//}
