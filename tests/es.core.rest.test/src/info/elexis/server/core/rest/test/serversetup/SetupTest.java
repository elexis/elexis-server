package info.elexis.server.core.rest.test.serversetup;

import static info.elexis.server.core.rest.test.AllTests.BASE_URL;
import static info.elexis.server.core.rest.test.AllTests.REST_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.UriType;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;
import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.RoleService;
import info.elexis.server.core.connector.elexis.services.UserService;
import info.elexis.server.core.rest.test.AllTests;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SetupTest {

	public static final String OAUTH_TOKEN_LOCATION = BASE_URL + "/openid/token";
	public static final String ELEXIS_SERVER_UNITTEST_CLIENT = "es-unittest-client";

	public static final String USER_PASS_PRACTITIONER = "practitioner";
	public static final String USER_PASS_ESADMIN = "esadmin";

	public static final String CONNECTOR_CONNECTION_LOCATION = "/elexis-connector/connection";

	private OkHttpClient client = AllTests.getDefaultOkHttpClient();
	private Gson gson = new Gson();
	private FhirContext fhirContext = FhirContext.forDstu3();
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private Request getDBConnectionRequest = new Request.Builder().url(REST_URL + CONNECTOR_CONNECTION_LOCATION)
			.build();
	private Request getDBStatusInformation = new Request.Builder()
			.url(REST_URL + CONNECTOR_CONNECTION_LOCATION + "/status").build();

	private Response response;

	@BeforeClass
	public static void waitForService() throws IOException, InterruptedException {
		int i = 0;
		do {
			Thread.sleep(500);
			System.out.println("Waiting for servlet ...");
			if (i++ == 50) {
				throw new IOException("Could not connect to servlet");
			}
		} while (!AllTests.isReachable(REST_URL + "/system/v1/uptime"));
	}

	@Test
	public void _01_startupWithoutDatabaseConnection() throws IOException {
		response = client.newCall(getDBConnectionRequest).execute();
		assertTrue(response.body().string(), response.isSuccessful());
		assertEquals(204, response.code());

		response = client.newCall(getDBStatusInformation).execute();
		String responseString = response.body().string();
		assertTrue(responseString, response.isSuccessful());
		assertEquals("Entity Manager is null.", responseString);
	}

	@Test
	public void _02_0_setInvalidTestDatabaseConnection() throws IOException {
		DBConnection dbc = new DBConnection();
		dbc.hostName = "localhost";
		dbc.connectionString = "jdbc:mysql://localhost:3306/invalidDatabase";
		dbc.port = "3306";
		dbc.password = "elexis";
		dbc.username = "elexis";
		dbc.rdbmsType = DBType.MySQL;

		String dbcJson = new Gson().toJson(dbc);
		RequestBody body = RequestBody.create(JSON, dbcJson);
		Request request = new Request.Builder().url(REST_URL + CONNECTOR_CONNECTION_LOCATION).post(body).build();
		response = client.newCall(request).execute();
		assertFalse(response.isSuccessful());
		assertEquals(422, response.code());
	}

	@Test
	public void _02_1_setValidTestDatabaseConnection() throws IOException {
		DBConnection dbc = AllTests.getTestDatabaseConnection();
		String dbcJson = new Gson().toJson(dbc);
		RequestBody body = RequestBody.create(JSON, dbcJson);
		Request request = new Request.Builder().url(REST_URL + CONNECTOR_CONNECTION_LOCATION).post(body).build();
		response = client.newCall(request).execute();
		assertTrue(response.body().string(), response.isSuccessful());

		response = client.newCall(getDBStatusInformation).execute();
		assertTrue(response.isSuccessful());
		assertTrue(response.body().string().startsWith("Elexis 3"));

		initializeTestUsersAndRoles();
	}

	private static void initializeTestUsersAndRoles() {
		Role esadminRole = new Role();
		esadminRole.setId("esadmin");
		esadminRole.setSystemRole(true);
		esadminRole = (Role) RoleService.save(esadminRole);

		Role fhirRole = new Role();
		fhirRole.setId("fhir");
		fhirRole.setSystemRole(true);
		fhirRole = (Role) RoleService.save(fhirRole);

		Kontakt drGonzo = new KontaktService.PersonBuilder("Oscar", "Zeta Acosta", LocalDate.of(1935, 4, 8),
				Gender.MALE).mandator().buildAndSave();

		User practitioner = new UserService.Builder(USER_PASS_PRACTITIONER, drGonzo).build();
		UserService.setPasswordForUser(practitioner, USER_PASS_PRACTITIONER);
		practitioner.setAllowExternal(true);
		practitioner.getRoles().add(fhirRole);
		UserService.save(practitioner);

		User esadmin = new UserService.Builder(USER_PASS_ESADMIN, drGonzo).build();
		UserService.setPasswordForUser(esadmin, USER_PASS_ESADMIN);
		esadmin.setAllowExternal(true);
		esadmin.getRoles().add(esadminRole);
		UserService.save(esadmin);
	}

	@Test
	public void _03_getDatabaseConnectionInformation() throws IOException, InterruptedException {
		response = client.newCall(getDBStatusInformation).execute();
		assertTrue(response.isSuccessful());
		assertTrue(response.body().string().startsWith("Elexis 3."));
	}

	@Test
	public void _04_tryToSetDatabaseConnectionWithoutAuthentication_Unauthorized() throws IOException {
		DBConnection dbc = AllTests.getTestDatabaseConnection();
		String dbcJson = gson.toJson(dbc);
		RequestBody body = RequestBody.create(JSON, dbcJson);
		Request request = new Request.Builder().url(REST_URL + CONNECTOR_CONNECTION_LOCATION).post(body).build();
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
		UriType extension = (UriType) security.getExtension().get(0).getExtension().get(0).getValue();
		assertEquals("http://localhost:8380/openid/token", extension.getValueAsString());
	}

	private static String bearerAccessToken_Scope_FHIR_Invalid; // non-static variables are null after each test
	private static String bearerAccessToken_Scope_FHIR_Valid;

	@Test
	public void _07_fetchAccessTokenWithUnitTestClientTestUsersForScopeFhir() throws Exception {
		OAuthClientRequest resourceOwnerPasswordRequest = OAuthClientRequest.tokenLocation(OAUTH_TOKEN_LOCATION)
				.setGrantType(GrantType.PASSWORD).setUsername(USER_PASS_PRACTITIONER)
				.setPassword(USER_PASS_PRACTITIONER).setScope("fhir").buildQueryMessage();

		OAuthJSONAccessTokenResponse accessToken = new URLConnectionClient().execute(resourceOwnerPasswordRequest,
				prepareUnitTestClientAuthorizationHeaders(), OAuth.HttpMethod.POST, OAuthJSONAccessTokenResponse.class);

		bearerAccessToken_Scope_FHIR_Valid = accessToken.getAccessToken();
		assertNotNull(accessToken);
		assertNotNull(bearerAccessToken_Scope_FHIR_Valid);
		assertTrue(accessToken.getExpiresIn() <= 3600l);
		assertEquals("Bearer", accessToken.getTokenType());

		resourceOwnerPasswordRequest = OAuthClientRequest.tokenLocation(OAUTH_TOKEN_LOCATION)
				.setGrantType(GrantType.PASSWORD).setUsername(USER_PASS_ESADMIN).setPassword(USER_PASS_ESADMIN)
				.setScope("fhir").buildQueryMessage();
		accessToken = new URLConnectionClient().execute(resourceOwnerPasswordRequest,
				prepareUnitTestClientAuthorizationHeaders(), OAuth.HttpMethod.POST, OAuthJSONAccessTokenResponse.class);

		bearerAccessToken_Scope_FHIR_Invalid = accessToken.getAccessToken();
		assertNotNull(accessToken);
		assertNotNull(bearerAccessToken_Scope_FHIR_Invalid);
		assertTrue(accessToken.getExpiresIn() <= 3600l);
		assertEquals("Bearer", accessToken.getTokenType());
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
				.addHeader("Authorization", "Bearer " + bearerAccessToken_Scope_FHIR_Valid).build();
		response = client.newCall(request).execute();
		assertEquals(HttpURLConnection.HTTP_OK, response.code());

		request = new Request.Builder().url(BASE_URL + "/fhir/Patient/s9b71824bf6b877701111")
				.addHeader("Authorization", "Bearer " + bearerAccessToken_Scope_FHIR_Invalid).build();
		response = client.newCall(request).execute();
		// user has requested scope fhir, but does not have role fhir in Elexis - hence
		// has to fail
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.code());
	}

	// Test access FHIR resource with fhir* permission only
}
