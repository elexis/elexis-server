package info.elexis.server.core.connector.elexis;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.test.initializer.TestDatabaseInitializer;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.core.connector.elexis.services.AllServiceTests;

@RunWith(Suite.class)
//@SuiteClasses({ AllServiceTests.class, AllBillingTests.class })
@SuiteClasses({
	AllServiceTests.class
})
public class AllTestsSuite {
	
	private static IModelService coreModelService;
	private static IElexisEntityManager entityManager;
	private static IConfigService configService;
	
	public static String RWA_ID;
	
	@BeforeClass
	public static void setupClass() throws IOException, SQLException{
		coreModelService = OsgiServiceUtil.getService(IModelService.class,
			"(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		assertNotNull(coreModelService);
		entityManager = OsgiServiceUtil.getService(IElexisEntityManager.class).get();
		assertNotNull(entityManager);
		configService = OsgiServiceUtil.getService(IConfigService.class).get();
		assertNotNull(configService);
		TestDatabaseInitializer initializer =
			new TestDatabaseInitializer(coreModelService, entityManager);
		assertNotNull(initializer);
		initializer.initializeDb(configService);
		
		//		AllTestsSuite.getInitializer().initializePatient();
		//		AllTestsSuite.getInitializer().initializeLaborTarif2009Tables();
		//		AllTestsSuite.getInitializer().initializeAgendaTable();
		//		AllTestsSuite.getInitializer().initializeArzttarifePhysioLeistungTables();
		//		AllTestsSuite.getInitializer().initializeTarmedTables();
		//		AllTestsSuite.getInitializer().initializeLaborItemsOrdersResults();
		//		AllTestsSuite.getInitializer().initializeReminders();
		//		AllTestsSuite.getInitializer().initializeLeistungsblockTables();
		//		AllTestsSuite.getInitializer().initializeLabResult();
		//		AllTestsSuite.getInitializer().initializeBehandlung();
		
		//		IStock rowaStock = new StockService.Builder("RWA", 0).build();
		//		rowaStock.setDriverUuid(MockStockCommissioningSystemDriverFactory.uuid.toString());
		//		rowaStock.setDriverConfig("10.10.20.30:6050;defaultOutputDestination=2");
		//		StockService.save(rowaStock);
		//		RWA_ID = rowaStock.getId();
	}
	
}
