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

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;

@RunWith(Suite.class)
@SuiteClasses({ SetupTest.class })
public class AllTests {
	
	@BeforeClass
	public static void setup() throws IOException, SQLException {
		TestDatabaseInitializer tdi = new TestDatabaseInitializer();
		tdi.initializeDb(getTestDatabaseConnection(), true);
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
	
	/**
	 * Overwritten from {@link ElexisDBConnection}, we need a db that can be contacted by both this
	 * server and openid
	 */
	public static DBConnection getTestDatabaseConnection() {
		DBConnection retVal = new DBConnection();
		retVal.connectionString = "jdbc:h2:~/elexis-server/elexisTest;AUTO_SERVER=TRUE";
		retVal.rdbmsType = DBType.H2;
		retVal.username = "sa";
		retVal.password = "";
		return retVal;
	}	
}
