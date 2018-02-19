package info.elexis.server.setup.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.apache.commons.codec.DecoderException;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.CapabilityStatement.CapabilityStatementRestSecurityComponent;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ca.uhn.fhir.context.FhirContext;
import ch.elexis.core.common.DBConnection;
import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.datasource.util.ElexisDBConnectionUtil;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.UserService;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SetupTest {

	public static final String BASE_URL = "http://localhost:8380";
	public static final String REST_URL = BASE_URL + "/services";

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
			System.out.println("Waiting for login servlet ...");
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
		assertTrue(response.isSuccessful());
		assertEquals(204, response.code());

		response = client.newCall(getDBStatusInformation).execute();
		assertTrue(response.isSuccessful());
		assertEquals("Entity Manager is null.", response.body().string());
	}

	@Test
	public void _02_setTestDatabaseConnection() throws IOException {
		DBConnection dbc = ElexisDBConnectionUtil.getTestDatabaseConnection();
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
	public void _04_tryToSetDatabaseConnectionWithoutAuthentication() throws IOException {
		DBConnection dbc = ElexisDBConnectionUtil.getTestDatabaseConnection();
		String dbcJson = gson.toJson(dbc);
		RequestBody body = RequestBody.create(JSON, dbcJson);
		Request request = new Request.Builder().url(REST_URL + "/elexis/connector/v1/connection").post(body).build();
		response = client.newCall(request).execute();
		assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, response.code());
	}

	@Test
	public void _05_tryToAccessFHIRPatientResourceWithoutAuthentication() throws IOException {
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

	@Test
	public void _07_dynamicRegisterOAuth2Client()
			throws NoSuchAlgorithmException, InvalidKeySpecException, DecoderException, IOException {
		Kontakt mandant = new KontaktService.PersonBuilder("Pickle", "Rick", LocalDate.of(1979, 07, 26), Gender.MALE)
				.mandator().buildAndSave();
		User prick = new UserService.Builder("prick", mandant).buildAndSave();
		UserService.setPasswordForUser(prick, "password");

		String credential = Credentials.basic("prick", "p4ssword");

		JsonObject obj = new JsonObject();
		obj.addProperty("client_name", "test_client");
		JsonArray grantTypes = new JsonArray();
		grantTypes.add("esadmin");
		obj.add("grant_types", grantTypes);
		JsonArray redirectUris = new JsonArray();
		redirectUris.add("localhost");
		obj.add("redirect_uris", grantTypes);
		System.out.println(obj.toString());
		RequestBody body = RequestBody.create(JSON, obj.toString());

		// DOES NOT REQUIRE AUTH, WHY??
		Request request = new Request.Builder().header("Authorization", credential).post(body)
				.url(BASE_URL + "/openid/register").build();
		response = client.newCall(request).execute();
		assertEquals(HttpURLConnection.HTTP_CREATED, response.code());
		System.out.println(response.body().string());
	}

	// Test access FHIR resource with fhir* permission only
}
