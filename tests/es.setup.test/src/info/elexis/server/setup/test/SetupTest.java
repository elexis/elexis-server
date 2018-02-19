package info.elexis.server.setup.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.binary.Base64;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;
import ch.elexis.core.common.DBConnection;
import info.elexis.server.core.connector.elexis.jpa.test.TestEntities;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SetupTest {

	 public static final String BASE_URL = "http://localhost:8381"; // Via TCP/IP
	// Eclipse Monitor
	// http://www.avajava.com/tutorials/lessons/how-do-i-monitor-http-communication-in-eclipse.html
//	public static final String BASE_URL = "http://localhost:8380";
	public static final String REST_URL = BASE_URL + "/services";

	public static final String OAUTH_TOKEN_LOCATION = BASE_URL + "/openid/token";
	public static final String ELEXIS_SERVER_UNITTEST_CLIENT = "es-unittest-client";

	private OkHttpClient client = new OkHttpClient();
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private Gson gson = new Gson();
	private FhirContext fhirContext = FhirContext.forDstu3();

	public SetupTest() {
		client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS).build();
	}

	@BeforeClass
	public static void waitForService() throws IOException, InterruptedException {
		do {
			Thread.sleep(500);
			System.out.println("Waiting for servlet ...");
		} while (!AllTests.isReachable(REST_URL + "/system/v1/uptime"));
	}

	private Request getDBConnectionRequest = new Request.Builder().url(REST_URL + "/elexis/connector/v1/connection")
			.build();
	private Request getDBStatusInformation = new Request.Builder().url(REST_URL + "/elexis/connector/v1/status")
			.build();

	private Response response;

	@Test
	public void _01_startupWithoutDatabaseConnection() throws IOException {
		response = client.newCall(getDBConnectionRequest).execute();
		assertTrue(response.body().string(), response.isSuccessful());
		assertEquals(204, response.code());

		response = client.newCall(getDBStatusInformation).execute();
		assertTrue(response.isSuccessful());
		assertEquals("Entity Manager is null.", response.body().string());
	}

	@Test
	public void _02_setTestDatabaseConnection() throws IOException {
		DBConnection dbc = AllTests.getTestDatabaseConnection();
		String dbcJson = new Gson().toJson(dbc);
		RequestBody body = RequestBody.create(JSON, dbcJson);
		Request request = new Request.Builder().url(REST_URL + "/elexis/connector/v1/connection").post(body).build();
		response = client.newCall(request).execute();
		assertTrue(response.body().string(), response.isSuccessful());
	}

	@Test
	public void _03_getDatabaseConnectionInformation() throws IOException, InterruptedException {
		response = client.newCall(getDBStatusInformation).execute();
		assertTrue(response.isSuccessful());
		assertTrue(response.body().string().startsWith("Elexis 3.2.0 DBv 3.2.7"));
	}

	@Test
	public void _04_tryToSetDatabaseConnectionWithoutAuthentication_Unauthorized() throws IOException {
		DBConnection dbc = AllTests.getTestDatabaseConnection();
		String dbcJson = gson.toJson(dbc);
		RequestBody body = RequestBody.create(JSON, dbcJson);
		Request request = new Request.Builder().url(REST_URL + "/elexis/connector/v1/connection").post(body).build();
		response = client.newCall(request).execute();
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.code());
	}

	@Test
	public void _05_tryToAccessFHIRPatientResourceWithoutAuthentication_Unauthorized() throws IOException {
		Request request = new Request.Builder().url(BASE_URL + "/fhir/Patient/s9b71824bf6b877701111").build();
		response = client.newCall(request).execute();
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.code());
	}

	@Test
	public void _06_accessFhirMetadataResourceForSmartOnFhirInformation() throws IOException {
		Request request = new Request.Builder().url(BASE_URL + "/fhir/metadata?_format=json").build();
		response = client.newCall(request).execute();
		assertEquals(HttpURLConnection.HTTP_OK, response.code());
		String body = response.body().string();
		CapabilityStatement cs = (CapabilityStatement) fhirContext.newJsonParser().parseResource(body);
		CapabilityStatementRestSecurityComponent security = cs.getRest().get(0).getSecurity();
		CodeableConcept securityServiceCoding = security.getService().get(0);
		assertEquals("http://hl7.org/fhir/restful-security-service",
				securityServiceCoding.getCoding().get(0).getSystem());
	}

	private static String bearerAccessToken_Scopes_FhirEsadmin; // non-static variables are null after each test
	private static String bearerAccessToken_Scopes_Esadmin;
	
	@Test
	public void _07_fetchAccessTokenWithUnitTestClientAndAdminUserForScopeFhirAndEsadmin() throws Exception {
		OAuthClientRequest resourceOwnerPasswordRequest = OAuthClientRequest.tokenLocation(OAUTH_TOKEN_LOCATION)
				.setGrantType(GrantType.PASSWORD).setUsername(TestEntities.USER_ADMINISTRATOR_ID)
				.setPassword(TestEntities.USER_ADMINISTRATOR_PASS).setScope("fhir esadmin").buildQueryMessage();
		OAuthJSONAccessTokenResponse accessToken = new URLConnectionClient().execute(resourceOwnerPasswordRequest,
				prepareUnitTestClientAuthorizationHeaders(), OAuth.HttpMethod.POST, OAuthJSONAccessTokenResponse.class);

		bearerAccessToken_Scopes_FhirEsadmin = accessToken.getAccessToken();
		assertNotNull(accessToken);
		assertNotNull(bearerAccessToken_Scopes_FhirEsadmin);
		assertTrue(accessToken.getExpiresIn() <= 3600l);
		assertEquals("Bearer", accessToken.getTokenType());
		assertEquals("fhir esadmin", accessToken.getScope());
		
		resourceOwnerPasswordRequest = OAuthClientRequest.tokenLocation(OAUTH_TOKEN_LOCATION)
				.setGrantType(GrantType.PASSWORD).setUsername(TestEntities.USER_USER_ID)
				.setPassword(TestEntities.USER_USER_PASS).setScope("esadmin").buildQueryMessage();
		accessToken = new URLConnectionClient().execute(resourceOwnerPasswordRequest,
				prepareUnitTestClientAuthorizationHeaders(), OAuth.HttpMethod.POST, OAuthJSONAccessTokenResponse.class);
		
		bearerAccessToken_Scopes_Esadmin = accessToken.getAccessToken();
		assertNotNull(accessToken);
		assertNotNull(bearerAccessToken_Scopes_FhirEsadmin);
		assertTrue(accessToken.getExpiresIn() <= 3600l);
		assertEquals("Bearer", accessToken.getTokenType());
		assertEquals("esadmin", accessToken.getScope());
		
		// TODO mapping role to scope
	}

	private Map<String, String> prepareUnitTestClientAuthorizationHeaders() {
		String basicAuthStr = ELEXIS_SERVER_UNITTEST_CLIENT + ":" + ELEXIS_SERVER_UNITTEST_CLIENT;
		String basicAuthStrEncoded = Base64.encodeBase64String(basicAuthStr.getBytes());
		Map<String, String> headers = new HashMap<String, String>();
		headers.put(OAuth.HeaderType.CONTENT_TYPE, OAuth.ContentType.URL_ENCODED);
		headers.put("Authorization", "Basic " + basicAuthStrEncoded);
		return headers;
	}

	@Test
	public void _08_tryToAccessFHIRPatientResourceWithAccessToken_scopeFhir() throws IOException {
		Request request = new Request.Builder().url(BASE_URL + "/fhir/Patient/s9b71824bf6b877701111")
				.addHeader("Authorization", "Bearer " + bearerAccessToken_Scopes_FhirEsadmin).build();
		response = client.newCall(request).execute();
		assertEquals(HttpURLConnection.HTTP_OK, response.code());
		
		request = new Request.Builder().url(BASE_URL + "/fhir/Patient/s9b71824bf6b877701111")
				.addHeader("Authorization", "Bearer " + bearerAccessToken_Scopes_Esadmin).build();
		response = client.newCall(request).execute();
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.code());
	}

	// Test access FHIR resource with fhir* permission only
}
