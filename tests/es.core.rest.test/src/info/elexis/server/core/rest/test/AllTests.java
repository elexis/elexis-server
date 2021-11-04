package info.elexis.server.core.rest.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import info.elexis.server.core.rest.test.elexisinstances.AllElexisInstancesTests;

@RunWith(Suite.class)
@SuiteClasses({
	AllElexisInstancesTests.class
})
public class AllTests {
	
	/**
	 * Eclipse Monitor
	 * http://www.avajava.com/tutorials/lessons/how-do-i-monitor-http-communication-in-eclipse.html
	 */
	// public static final String BASE_URL = "http://localhost:8381"; // Via TCP/IP
	
	public static final String BASE_URL = "http://localhost:8380";
	public static final String REST_URL = BASE_URL + "/services";
	
//	private static OkHttpClient defaultOkHttpClient =
//		new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
//			.writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build();
//	
//	public static boolean isReachable(String targetUrl) throws MalformedURLException, IOException{
//		HttpURLConnection httpUrlConnection =
//			(HttpURLConnection) new URL(targetUrl).openConnection();
//		httpUrlConnection.setRequestMethod("GET");
//		try {
//			int responseCode = httpUrlConnection.getResponseCode();
//			
//			return responseCode > 0;
//		} catch (Exception exception) {
//			return false;
//		}
//	}
//	
//	public static OkHttpClient getDefaultOkHttpClient(){
//		return defaultOkHttpClient;
//	}
//	
//	private static TestDatabaseConnection tbc;
//	
//	public static synchronized DBConnection getTestDatabaseConnection() throws SQLException{
//		if (tbc == null) {
//			tbc = new TestDatabaseConnection(true);
//		}
//		return tbc;
//	}
}
