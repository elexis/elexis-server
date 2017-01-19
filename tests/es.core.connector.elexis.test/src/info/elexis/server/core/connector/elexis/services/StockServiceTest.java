package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IStockEntry;
import ch.elexis.core.services.IStockService.Availability;
import info.elexis.server.core.connector.elexis.AllTestsSuite;
import info.elexis.server.core.connector.elexis.jpa.StoreToStringService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.StockEntry;
import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;

public class StockServiceTest extends AbstractServiceTest {

	private static Stock defaultStock;

	private static StockService stockService = new StockService();
	private static ArtikelstammItem article_A;
	private static ArtikelstammItem item2, item3;
	private static String artikel_AStS;
	private static String artikel_item2, artikel_item3;

	@BeforeClass
	public static void init() {
		AllTestsSuite.getInitializer().initializeArtikelstamm();

		article_A = ArtikelstammItemService.load(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID).get();
		artikel_AStS = StoreToStringService.storeToString(article_A);

		item2 = ArtikelstammItemService.load(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID_2).get();
		artikel_item2 = StoreToStringService.storeToString(item2);

		item3 = ArtikelstammItemService.load(TestEntities.ARTIKELSTAMM_ITEM_PHARMA_ID_3).get();
		artikel_item3 = StoreToStringService.storeToString(item3);

		defaultStock = StockService.load("STD").get();

		IStockEntry stockEntry_A = stockService.storeArticleInStock(defaultStock, artikel_AStS);
		stockEntry_A.setMinimumStock(5);
		stockEntry_A.setCurrentStock(10);
		stockEntry_A.setMaximumStock(15);
		StockEntryService.save((StockEntry) stockEntry_A);
	}

	@Test
	public void testStoreUnstoreFindPreferredArticleInStock() {
		List<? extends IStockEntry> a_entries = stockService.findAllStockEntriesForArticle(artikel_AStS);
		assertEquals(1, a_entries.size());
	}

	@Test
	public void testPerformDisposalAndReturnOfArticle() {
		ArtikelstammItem artikelstammItem = ArtikelstammItemService.findByGTIN("7680531600264").get();

		Stock stock = new StockService.Builder("TMP", 20).buildAndSave();
		IStockEntry se = stockService.storeArticleInStock(stock, StoreToStringService.storeToString(artikelstammItem));
		se.setMinimumStock(15);
		se.setCurrentStock(13);
		StockEntryService.save((StockEntry) se);

		stockService.performSingleDisposal(artikelstammItem, 5, null);
		IStockEntry prefSE = stockService
				.findPreferredStockEntryForArticle(StoreToStringService.storeToString(artikelstammItem), null);

		assertEquals(((StockEntry) se).getId(), ((StockEntry) prefSE).getId());
		assertEquals(8, prefSE.getCurrentStock());
		assertEquals(15, prefSE.getMinimumStock());

		stockService.performSingleReturn(artikelstammItem, 3, null);
		prefSE = stockService.findPreferredStockEntryForArticle(StoreToStringService.storeToString(artikelstammItem),
				null);
		assertEquals(((StockEntry) se).getId(), ((StockEntry) prefSE).getId());
		assertEquals(11, prefSE.getCurrentStock());
		assertEquals(15, prefSE.getMinimumStock());

		List<StockEntry> entries = StockService.load(stock.getId()).get().getEntries();
		assertEquals(1, entries.size());

		StockService.remove(StockService.load(stock.getId()).get());
	}

	@Test
	public void testGetCummulatedCountForArticle() {
		Stock stock = new StockService.Builder("TMP", 20).buildAndSave();
		IStockEntry stockEntry_B = stockService.storeArticleInStock(stock, artikel_AStS);
		stockEntry_B.setMinimumStock(5);
		stockEntry_B.setCurrentStock(10);
		stockEntry_B.setMaximumStock(15);
		StockEntryService.save((StockEntry) stockEntry_B);

		IStockEntry stockEntry_C = stockService.storeArticleInStock(stock, artikel_item2);
		stockEntry_C.setMinimumStock(15);
		stockEntry_C.setCurrentStock(10);
		stockEntry_C.setMaximumStock(20);
		StockEntryService.save((StockEntry) stockEntry_C);

		IStockEntry stockEntry_D = stockService.storeArticleInStock(stock, artikel_item3);
		stockEntry_D.setMinimumStock(5);
		stockEntry_D.setCurrentStock(5);
		stockEntry_D.setMaximumStock(20);
		StockEntryService.save((StockEntry) stockEntry_D);

		IStockEntry stockEntry_Dd = stockService.storeArticleInStock(defaultStock, artikel_item3);
		stockEntry_Dd.setMinimumStock(15);
		stockEntry_Dd.setCurrentStock(0);
		stockEntry_Dd.setMaximumStock(20);
		StockEntryService.save((StockEntry) stockEntry_Dd);

		Integer cumulatedStockForArticle = stockService.getCumulatedStockForArticle(article_A);
		assertEquals(20, cumulatedStockForArticle.intValue());

		Availability availability = stockService.getCumulatedAvailabilityForArticle(article_A);
		assertEquals(Availability.IN_STOCK, availability);

		Availability availability3 = stockService.getCumulatedAvailabilityForArticle(item2);
		assertEquals(Availability.CRITICAL_STOCK, availability3);

		Availability availability4 = stockService.getCumulatedAvailabilityForArticle(item2);
		assertEquals(Availability.CRITICAL_STOCK, availability4);
	}

}
