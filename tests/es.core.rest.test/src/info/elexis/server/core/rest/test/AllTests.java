package info.elexis.server.core.rest.test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.jpa.test.TestDatabaseInitializer;
import info.elexis.server.core.rest.test.elexisinstances.AllElexisInstancesTests;
import info.elexis.server.core.rest.test.serversetup.SetupTest;
import info.elexis.server.core.rest.test.swagger.SwaggerTest;
import okhttp3.OkHttpClient;

@RunWith(Suite.class)
@SuiteClasses({ SetupTest.class, AllElexisInstancesTests.class, SwaggerTest.class })
public class AllTests {

	/**
	 * Eclipse Monitor
	 * http://www.avajava.com/tutorials/lessons/how-do-i-monitor-http-communication-in-eclipse.html
	 */
	// public static final String BASE_URL = "http://localhost:8381"; // Via TCP/IP

	public static final String BASE_URL = "http://localhost:8380";
	public static final String REST_URL = BASE_URL + "/services";

	private static OkHttpClient defaultOkHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
			.writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();

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
	 * Overwritten from {@link ElexisDBConnection}, we need a db that can be
	 * contacted by both this server and openid
	 */
	public static DBConnection getTestDatabaseConnection() {
		DBConnection retVal = new DBConnection();
		retVal.connectionString = "jdbc:h2:~/elexis-server/elexisTest;AUTO_SERVER=TRUE";
		retVal.rdbmsType = DBType.H2;
		retVal.username = "sa";
		retVal.password = "";
		return retVal;
	}

	public static OkHttpClient getDefaultOkHttpClient() {
		return defaultOkHttpClient;
	}
}
