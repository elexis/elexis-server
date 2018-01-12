package info.elexis.server.setup.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.elexis.server.core.connector.elexis.datasource.util.ElexisDBConnectionUtil;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

@RunWith(Suite.class)
@SuiteClasses({ SetupTest.class })
public class AllTests {
	
	@BeforeClass
	public static void setup() throws IOException, SQLException {
		TestDatabaseInitializer tdi = new TestDatabaseInitializer();
		tdi.initializeDb(ElexisDBConnectionUtil.getTestDatabaseConnection(), true);
	}
	

	public static boolean isReachable(String targetUrl) throws MalformedURLException, IOException {
		HttpURLConnection httpUrlConnection = (HttpURLConnection) new URL(targetUrl).openConnection();
		httpUrlConnection.setRequestMethod("GET");
		try {
			int responseCode = httpUrlConnection.getResponseCode();

			return responseCode > 0;
		} catch (Exception exception) {
			return false;
		}
	}

}
