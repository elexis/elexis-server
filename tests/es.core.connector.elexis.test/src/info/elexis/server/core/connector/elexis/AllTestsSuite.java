package info.elexis.server.core.connector.elexis;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.elexis.server.core.connector.elexis.billable.BillingTest;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTest;
import info.elexis.server.core.connector.elexis.jpa.AbstractDBObjectIdDeletedTest;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.core.connector.elexis.services.AllServiceTests;

@RunWith(Suite.class)
@SuiteClasses({ AbstractDBObjectIdDeletedTest.class, AllServiceTests.class, BillingTest.class, VerrechenbarTest.class })
public class AllTestsSuite {

	private static TestDatabaseInitializer initializer = new TestDatabaseInitializer();

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		initializer.initializeDb();

		AllTestsSuite.getInitializer().initializeLaborTarif2009Tables();
		AllTestsSuite.getInitializer().initializeAgendaTable();
		AllTestsSuite.getInitializer().initializeArzttarifePhysioLeistungTables();
		AllTestsSuite.getInitializer().initializeTarmedTables();
		AllTestsSuite.getInitializer().initializeLaborItemsOrdersResults();
		AllTestsSuite.getInitializer().initializeReminders();
	}

	public static TestDatabaseInitializer getInitializer() {
		return initializer;
	}
}
