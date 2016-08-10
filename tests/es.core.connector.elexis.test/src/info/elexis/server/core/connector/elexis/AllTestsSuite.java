package info.elexis.server.core.connector.elexis;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.core.connector.elexis.services.KontaktServiceTest;

@RunWith(Suite.class)
@SuiteClasses({ KontaktServiceTest.class })
public class AllTestsSuite {

	@BeforeClass
	public static void setupClass() {
		TestDatabaseInitializer initializer = new TestDatabaseInitializer();
		initializer.initializeDb();
	}
	
}
