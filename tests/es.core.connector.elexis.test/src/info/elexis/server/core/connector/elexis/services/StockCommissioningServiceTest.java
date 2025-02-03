package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IStock;
import ch.elexis.core.model.builder.IArticleBuilder;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StockCommissioningServiceHolder;
import ch.elexis.core.types.ArticleTyp;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class StockCommissioningServiceTest {

	private static IModelService coreModelService;
	private static IStockService stockService;

	private static IPatient patient;
	private static IStock rwaStock;
	private static IArticle article;

	@BeforeClass
	public static void beforeClass() {

		coreModelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		stockService = OsgiServiceUtil.getService(IStockService.class).get();

		patient = new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), "Max", "Mustermann",
				LocalDate.now().minusYears(20), Gender.MALE).build();
		patient.setPatientNr("100");
		CoreModelServiceHolder.get().save(patient);
		coreModelService.save(stockService.getOrCreatePatientStock(patient));

		rwaStock = coreModelService.create(IStock.class);
		rwaStock.setPriority(1);
		rwaStock.setCode("RWA");
		rwaStock.setDriverConfig("192.168.54.58:6055;defaultOutputDestination=2");
		rwaStock.setDriverUuid("e02d0db0-f479-4f23-82c7-5e29e83d5f6b");
		rwaStock.setId("123456789");

		article = new IArticleBuilder(coreModelService, "myArticle", "123456789", ArticleTyp.ARTIKEL).build();
		article.setGtin("7680475050545");
		coreModelService.save(article);

		stockService.storeArticleInStock(rwaStock, article);
		coreModelService.save(rwaStock);
	}

	@Test
	public void syncImportetRwaArticles() {
		ArrayList<String> lGtin = new ArrayList<String>();
		// represents a patient article
		lGtin.add(patient.getId() + ":7680475050545");
		lGtin.add("7680481641003");

		StockCommissioningServiceHolder.get().synchronizeInventory(rwaStock, lGtin, null);
		assertEquals(rwaStock.getStockEntries().size(), 1);
		assertEquals(rwaStock.getStockEntries().get(0).getRwaStockLink(),
				"PatientStock-100");
	}

	@Test
	public void exportArticleFromROWA() {
		rwaStock.getStockEntries().get(0).setRwaStockLink(stockService.getPatientStock(patient).get());
		StockCommissioningServiceHolder.get().performArticleOutlay(rwaStock.getStockEntries().get(0), 1, null);

		assertEquals(rwaStock.getStockEntries().size(), 0);
		assertEquals(stockService.getPatientStock(patient).get().getStockEntries().size(), 0);
	}
}
