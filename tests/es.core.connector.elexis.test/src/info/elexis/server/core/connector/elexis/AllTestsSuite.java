package info.elexis.server.core.connector.elexis;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.elexis.server.core.connector.elexis.billable.AllBillingTests;
import info.elexis.server.core.connector.elexis.jpa.AbstractDBObjectIdDeletedTest;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Stock;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.core.connector.elexis.mocks.MockStockCommissioningSystemDriverFactory;
import info.elexis.server.core.connector.elexis.services.AllServiceTests;
import info.elexis.server.core.connector.elexis.services.StockService;

@RunWith(Suite.class)
@SuiteClasses({ AbstractDBObjectIdDeletedTest.class, AllServiceTests.class, AllBillingTests.class })
public class AllTestsSuite {

	private static TestDatabaseInitializer initializer = new TestDatabaseInitializer();
	public static String RWA_ID;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		initializer.initializeDb();

		AllTestsSuite.getInitializer().initializeLaborTarif2009Tables();
		AllTestsSuite.getInitializer().initializeAgendaTable();
		AllTestsSuite.getInitializer().initializeArzttarifePhysioLeistungTables();
		AllTestsSuite.getInitializer().initializeTarmedTables();
		AllTestsSuite.getInitializer().initializeLaborItemsOrdersResults();
		AllTestsSuite.getInitializer().initializeReminders();
		AllTestsSuite.getInitializer().initializeLeistungsblockTables();
		AllTestsSuite.getInitializer().initializeBehandlung();

		Stock rowaStock = new StockService.Builder("RWA", 0).build();
		rowaStock.setDriverUuid(MockStockCommissioningSystemDriverFactory.uuid.toString());
		rowaStock.setDriverConfig("10.10.20.30:6050;defaultOutputDestination=2");
		StockService.save(rowaStock);
		RWA_ID = rowaStock.getId();
	}

	public static TestDatabaseInitializer getInitializer() {
		return initializer;
	}
}
