package es.core.test.security.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;

import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import info.elexis.server.core.constants.SecurityConstants;
import info.elexis.server.core.security.LocalUserUtil;
import info.elexis.server.core.security.oauth2.ClientUtil;

public class TokenEndpointTest {

	public static final String TOKEN_LOCATION = "http://localhost:8380/login/oauth/token";
	public static final String PROTECTED_RESOURCE_LOCATION = "http://localhost:8380/services/system/v1/protected";
	public static final String CLIENT_ID_ADMIN_SCOPE = "Unit-Test-Client-AdminScope";
	public static String CLIENT_SECRET_ADMIN_SCOPE;

	@Rule
	public final ExpectedException thrown = ExpectedException.none();

	@BeforeClass
	public static void waitForService() throws IOException, InterruptedException {

		CLIENT_SECRET_ADMIN_SCOPE = ClientUtil.addOrReplaceOauthClient(CLIENT_ID_ADMIN_SCOPE,
				Collections.singleton(SecurityConstants.ES_ADMIN));
		assertNotNull(CLIENT_SECRET_ADMIN_SCOPE);

		do {
			Thread.sleep(500);
			System.out.println("Waiting for login servlet ...");
		} while (!isReachable(TOKEN_LOCATION));
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

	@Test
	public void testProtectedResourceNotAuthorized() throws Exception {
		HttpURLConnection resource = (HttpURLConnection) (new URL(PROTECTED_RESOURCE_LOCATION).openConnection());
		assertEquals(401, resource.getResponseCode());
	}

	@Test
	public void testClientCredentialsFlowOk() throws OAuthSystemException, OAuthProblemException, IOException {

		OAuthClientRequest request = OAuthClientRequest.tokenLocation(TOKEN_LOCATION)
				.setGrantType(GrantType.CLIENT_CREDENTIALS).setScope(SecurityConstants.ES_ADMIN)
				.setClientId(CLIENT_ID_ADMIN_SCOPE).setClientSecret(CLIENT_SECRET_ADMIN_SCOPE).buildQueryMessage();

		// create OAuth client that uses custom http client under the hood
		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

		OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);
		String accessToken = oAuthResponse.getAccessToken();

		HttpURLConnection resource = (HttpURLConnection) (new URL(PROTECTED_RESOURCE_LOCATION).openConnection());
		resource.addRequestProperty("Authorization", "Bearer " + accessToken);
		assertEquals(200, resource.getResponseCode());
	}

	@Test
	public void testClientCredentialsFlowFailWrongClientSecret() throws Exception {
		OAuthClientRequest request = OAuthClientRequest.tokenLocation(TOKEN_LOCATION)
				.setGrantType(GrantType.CLIENT_CREDENTIALS).setClientId(CLIENT_ID_ADMIN_SCOPE)
				.setClientSecret("Wr0ngSecret").buildQueryMessage();

		thrown.expect(OAuthProblemException.class);
		thrown.expectMessage("invalid client id or client secret");

		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		oAuthClient.accessToken(request);
	}

	@Test
	public void testClientCredentialsFlowFailInvalidScopeRequested() throws Exception {
		OAuthClientRequest request = OAuthClientRequest.tokenLocation(TOKEN_LOCATION)
				.setGrantType(GrantType.CLIENT_CREDENTIALS)
				.setScope(SecurityConstants.ES_ADMIN + " master0ftheUniverse").setClientId(CLIENT_ID_ADMIN_SCOPE)
				.setClientSecret(CLIENT_SECRET_ADMIN_SCOPE).buildQueryMessage();

		thrown.expect(OAuthProblemException.class);
		thrown.expectMessage("invalid client scope(s) requested");

		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		oAuthClient.accessToken(request);
	}

	/**
	 * client is valid, but no scope requested
	 * 
	 * @throws Exception
	 */
	@Test
	public void testClientCredentialsFlowFailInvalidNoScopeRequested() throws Exception {
		OAuthClientRequest request = OAuthClientRequest.tokenLocation(TOKEN_LOCATION)
				.setGrantType(GrantType.CLIENT_CREDENTIALS).setClientId(CLIENT_ID_ADMIN_SCOPE)
				.setClientSecret(CLIENT_SECRET_ADMIN_SCOPE).buildQueryMessage();

		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
		oAuthClient.accessToken(request);
		OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);
		String accessToken = oAuthResponse.getAccessToken();

		HttpURLConnection resource = (HttpURLConnection) (new URL(PROTECTED_RESOURCE_LOCATION).openConnection());
		resource.addRequestProperty("Authorization", "Bearer " + accessToken);
		assertEquals(403, resource.getResponseCode());
	}

	/**
	 * Client has the required scope rights, user does to
	 */
	@Test
	public void testResourceOwnerPasswordClientCredentialsFlowOk()
			throws OAuthSystemException, OAuthProblemException, IOException {
		
		LocalUserUtil.addOrReplaceLocalUser("localUser", "password", Collections.singleton(SecurityConstants.ES_ADMIN));

		OAuthClientRequest request = OAuthClientRequest.tokenLocation(TOKEN_LOCATION)
				.setGrantType(GrantType.PASSWORD)
				.setClientId(CLIENT_ID_ADMIN_SCOPE)
				.setClientSecret(CLIENT_SECRET_ADMIN_SCOPE)
				.setUsername("localUser")
				.setPassword("password2")
				.setScope(SecurityConstants.ES_ADMIN)
				.buildQueryMessage();

		// create OAuth client that uses custom http client under the hood
		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

		OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);
		String accessToken = oAuthResponse.getAccessToken();

		HttpURLConnection resource = (HttpURLConnection) (new URL(PROTECTED_RESOURCE_LOCATION).openConnection());
		resource.addRequestProperty("Authorization", "Bearer " + accessToken);
		assertEquals(200, resource.getResponseCode());
	}
}
