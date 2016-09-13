package info.elexis.server.core.connector.elexis;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.elexis.server.core.connector.elexis.billable.BillingTest;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.core.connector.elexis.services.ArtikelServiceTest;
import info.elexis.server.core.connector.elexis.services.DocHandleServiceTest;
import info.elexis.server.core.connector.elexis.services.KontaktServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ KontaktServiceTest.class, BillingTest.class, DocHandleServiceTest.class, ArtikelServiceTest.class })
public class AllTestsSuite {

	private static TestDatabaseInitializer initializer = new TestDatabaseInitializer();

	@BeforeClass
	public static void setupClass() {
		initializer.initializeDb();
	}

	public static TestDatabaseInitializer getInitializer() {
		return initializer;
	}
}
