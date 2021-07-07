package info.elexis.server.core.security.oauth2.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ch.elexis.core.utils.CoreUtil;
import info.elexis.server.core.Host;

public class OAuth2ClientService {

	private Logger logger = LoggerFactory.getLogger(OAuth2ClientService.class);

	private Map<String, TokenCacheObject> authCache = new HashMap<>();
	private int defaultExpireTime = 300000; // 5 minutes in milliseconds
	private boolean forceCacheExpireTime = false; // force removal of cached tokens based on default expire time
	private boolean cacheNonExpiringTokens = false;
	private boolean cacheTokens = true;

	private String introspectionEndpointBasicAuthHeaderValue;

	/**
	 * Call the OpenID introspect end-point to check the provided access token
	 * 
	 * @param accessToken
	 * @return
	 */
	public boolean checkAccessToken(String accessToken) {
		// First check if the in memory cache has an Authentication object, and
		// that it is still valid
		// If Valid, return true
		TokenCacheObject cacheAuth = checkCache(accessToken);
		if (cacheAuth != null) {
			return true;
		} else {
			cacheAuth = parseToken(accessToken);
			if (cacheAuth != null) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Retrieve the scopes for the provided access token
	 * 
	 * @param principals
	 * @return
	 */
	public Set<String> getScopes(String accessToken) {

		TokenCacheObject cacheAuth = checkCache(accessToken);
		if (cacheAuth != null) {
			return cacheAuth.token.getScope();
		} else {
			cacheAuth = parseToken(accessToken);
			if (cacheAuth != null) {
				return cacheAuth.token.getScope();
			} else {
				return Collections.emptySet();
			}
		}
	}

	private String queryIntrospectEndpoint(String accessToken) {

		try {
			if (introspectionEndpointBasicAuthHeaderValue == null) {
				initIntrospectionEndpointBasicAuthHeaderValue();
			}

			List<NameValuePair> form = new ArrayList<>();
			form.add(new BasicNameValuePair("token", accessToken));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form, Consts.UTF_8);

			try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
				HttpPost httpPost = new HttpPost(Host.getLocalhostBaseUrl() + "openid/introspect");
				httpPost.setHeader("Authorization", introspectionEndpointBasicAuthHeaderValue);
				httpPost.setEntity(entity);
				ResponseHandler<String> responseHandler = response -> {
					int status = response.getStatusLine().getStatusCode();
					if (status == 200) {
						HttpEntity responseEntity = response.getEntity();
						return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
					} else {
						logger.warn("queryIntrospectEndpoint [{}]", response);
						return null;
					}
				};
				return client.execute(httpPost, responseHandler);
			}

		} catch (IOException e) {
			logger.warn("queryIntrospectEndpoint", e);
			return null;
		}
	}

	/**
	 * Initialize the basic <code>Authorization</code> header value for the introspection endpoint.
	 * This header is to be passed on every call
	 * 
	 * @throws IOException
	 */
	private void initIntrospectionEndpointBasicAuthHeaderValue() throws IOException {
		Path fileName = CoreUtil.getElexisServerHomeDirectory().resolve("es-introspection-client.auth");
		List<String> readAllLines = Files.readAllLines(fileName);
		for (String credentialLine : readAllLines) {
			if (!credentialLine.startsWith("#")) {
				introspectionEndpointBasicAuthHeaderValue = "Basic "+Base64.encodeBase64String(credentialLine.getBytes());
			}
		}
	}

	/**
	 * Check to see if the introspection end point response for a token has been
	 * cached locally This call will return the token if it has been cached and is
	 * still valid according to the cache expire time on the TokenCacheObject. If a
	 * cached value has been found but is expired, either by default expire times or
	 * the token's own expire time, then the token is removed from the cache and
	 * null is returned.
	 * 
	 * @param key
	 *            is the token to check
	 * @return the cached TokenCacheObject or null
	 */
	private TokenCacheObject checkCache(String key) {
		if (cacheTokens && authCache.containsKey(key)) {
			TokenCacheObject tco = authCache.get(key);

			if (tco != null && tco.cacheExpire != null && tco.cacheExpire.after(new Date())) {
				return tco;
			} else {
				// if the token is expired, don't keep things around.
				authCache.remove(key);
			}
		}
		return null;
	}

	/**
	 * Validate a token string against the introspection endpoint, then parse it and
	 * store it in the local cache if caching is enabled.
	 *
	 * @param accessToken
	 *            Token to pass to the introspection endpoint
	 * @return TokenCacheObject containing authentication and token if the token was
	 *         valid, otherwise null
	 */
	private TokenCacheObject parseToken(String accessToken) {

		String validatedToken = queryIntrospectEndpoint(accessToken);

		if (validatedToken != null) {
			// parse the json
			JsonElement jsonRoot = new JsonParser().parse(validatedToken);
			if (!jsonRoot.isJsonObject()) {
				return null; // didn't get a proper JSON object
			}

			JsonObject tokenResponse = jsonRoot.getAsJsonObject();

			if (tokenResponse.get("error") != null) {
				// report an error?
				logger.error("Got an error back: " + tokenResponse.get("error") + ", "
						+ tokenResponse.get("error_description"));
				return null;
			}

			if (!tokenResponse.get("active").getAsBoolean()) {
				// non-valid token
				logger.info("Server returned non-active token");
				return null;
			}

			// create an OAuth2AccessToken
			OAuth2AccessToken token = createAccessToken(tokenResponse, accessToken);

			if (token.getExpiration() == null || token.getExpiration().after(new Date())) {
				// Store them in the cache
				TokenCacheObject tco = new TokenCacheObject(token);
				if (cacheTokens && (cacheNonExpiringTokens || token.getExpiration() != null)) {
					authCache.put(accessToken, tco);
				}
				return tco;
			}
		}

		// when the token is invalid for whatever reason
		return null;
	}

	private OAuth2AccessToken createAccessToken(final JsonObject token, final String tokenString) {
		OAuth2AccessToken accessToken = new OAuth2AccessTokenImpl(token, tokenString);
		return accessToken;
	}

	// Inner class to store in the hash map
	private class TokenCacheObject {
		OAuth2AccessToken token;
		Date cacheExpire;

		private TokenCacheObject(OAuth2AccessToken token) {
			this.token = token;

			// we don't need to check the cacheTokens values, because this won't actually be
			// added to the cache if cacheTokens is false
			// if the token isn't null we use the token expire time
			// if forceCacheExpireTime is also true, we also make sure that the token expire
			// time is shorter than the default expire time
			if ((this.token.getExpiration() != null) && (!forceCacheExpireTime || (forceCacheExpireTime
					&& (this.token.getExpiration().getTime() - System.currentTimeMillis() <= defaultExpireTime)))) {
				this.cacheExpire = this.token.getExpiration();
			} else { // if the token doesn't have an expire time, or if the using
						// forceCacheExpireTime the token expire time is longer than the default, then
						// use the default expire time
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.MILLISECOND, defaultExpireTime);
				this.cacheExpire = cal.getTime();
			}
		}
	}

	protected OAuth2AccessToken getIntrospectionToken(String accessToken) {
		TokenCacheObject tokenCacheObject = checkCache(accessToken);
		if (tokenCacheObject != null) {
			return tokenCacheObject.token;
		} else {
			tokenCacheObject = parseToken(accessToken);
			if (tokenCacheObject != null) {
				return tokenCacheObject.token;
			} 
		}
		throw new IllegalStateException("Could not find OAuth2AccessToken in cache");
	}
}
