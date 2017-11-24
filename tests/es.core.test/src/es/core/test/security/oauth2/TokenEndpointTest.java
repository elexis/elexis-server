package es.core.test.security.oauth2;

import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.junit.Test;

import info.elexis.server.core.constants.SecurityConstants;
import info.elexis.server.core.security.oauth2.ClientUtil;

public class TokenEndpointTest {

	public static final String TOKEN_LOCATION = "http://localhost:8380/login/oauth/token";

	public static final String PROTECTED_RESOURCE_LOCATION = "http://localhost:8380/services/system/v1/protected";

	public static final String CLIENT_ID = "Unit-Test-Client";
	public static String CLIENT_SECRET;

	@BeforeClass
	public static void waitForService() throws IOException, InterruptedException {

		CLIENT_SECRET = ClientUtil.addOrReplaceOauthClient(CLIENT_ID,
				Collections.singleton(SecurityConstants.ES_ADMIN));
		assertNotNull(CLIENT_SECRET);

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
	public void testClientCredentialsFlow() throws OAuthSystemException, OAuthProblemException, IOException {

		OAuthClientRequest request = OAuthClientRequest.tokenLocation(TOKEN_LOCATION)
				.setGrantType(GrantType.CLIENT_CREDENTIALS).setScope(SecurityConstants.ES_ADMIN).setClientId(CLIENT_ID)
				.setClientSecret(CLIENT_SECRET).buildQueryMessage();

		// create OAuth client that uses custom http client under the hood
		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

		OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);

		String accessToken = oAuthResponse.getAccessToken();

		HttpURLConnection resource_cxn = (HttpURLConnection) (new URL(PROTECTED_RESOURCE_LOCATION).openConnection());
		resource_cxn.addRequestProperty("Authorization", "Bearer " + accessToken);

		InputStream resource = resource_cxn.getInputStream();

		// Do whatever you want to do with the contents of resource at this point.

		BufferedReader r = new BufferedReader(new InputStreamReader(resource, "UTF-8"));
		String line = null;
		while ((line = r.readLine()) != null) {
			System.out.println(line);
		}

		r.close();
	}

	@Test
	public void testClientCredentialsFlowFail() throws OAuthSystemException, OAuthProblemException, IOException {

		OAuthClientRequest request = OAuthClientRequest.tokenLocation(TOKEN_LOCATION)
				.setGrantType(GrantType.CLIENT_CREDENTIALS).setClientId(CLIENT_ID).setClientSecret("Wr0ngSecret")
				.buildQueryMessage();

		// create OAuth client that uses custom http client under the hood
		OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

		OAuthJSONAccessTokenResponse oAuthResponse = oAuthClient.accessToken(request);

		String accessToken = oAuthResponse.getAccessToken();

	}

}
