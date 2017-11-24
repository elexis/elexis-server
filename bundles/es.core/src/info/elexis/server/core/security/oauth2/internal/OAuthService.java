package info.elexis.server.core.security.oauth2.internal;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.oltu.oauth2.common.message.OAuthResponse.OAuthResponseBuilder;

import info.elexis.server.core.security.oauth2.ClientUtil;

public class OAuthService {

	private static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 3600;

	private static final Map<String, TokenMapEntry> tokenMap = Collections
			.synchronizedMap(new PassiveExpiringMap<String, TokenMapEntry>(ACCESS_TOKEN_EXPIRATION_SECONDS * 1000));

	public boolean refreshTokenSupported() {
		return false;
	}

	public long getExpireIn(String accessToken) {
		return ACCESS_TOKEN_EXPIRATION_SECONDS;
	}

	public boolean checkAccessToken(String accessToken, HttpServletRequest httpServletRequest) {
		if (tokenMap.containsKey(accessToken)) {
			TokenMapEntry tokenMapEntry = tokenMap.get(accessToken);
			return tokenMapEntry.isValid();
		}
		return false;
	}

	/**
	 * Add access token for a client credentials flow given a specific set of
	 * scopes.
	 * 
	 * @param accessToken
	 * @param clientId
	 * @param clientSecret
	 * @param scopes
	 * @return <code>null</code> if no error
	 */
	public OAuthResponseBuilder addAcessTokenClientCredentials(String accessToken, String clientId, String clientSecret,
			Set<String> scopes) {

		// check client or fail
		if (!ClientUtil.validateClient(clientId, clientSecret)) {
			return ResponseUtils.responseUnauthClient("invalid client id or client secret");
		}
		// check scopes (roles) or fail
		if (!ClientUtil.validateRequestedScopes(clientId, scopes)) {
			return ResponseUtils.responseInvalidScope("invalid client scope(s) requested");
		}
		// add access token
		TokenMapEntry tme = new TokenMapEntry(accessToken,
				LocalDateTime.now().plusSeconds(ACCESS_TOKEN_EXPIRATION_SECONDS), scopes, clientId);
		tokenMap.put(accessToken, tme);

		return null;
	}

	private boolean checkScopes(String clientId, Set<String> scopes) {
		// TODO Auto-generated method stub
		return false;
	}

	public OAuthResponseBuilder addAcessTokenResourceOwnerPasswordClientCredentials(String accessToken, String username,
			String password, String clientId, String clientSecret, Set<String> scopes) {
		// check client or fail
		if (!ClientUtil.validateClient(clientId, clientSecret)) {
			return ResponseUtils.responseUnauthClient("invalid client id or client secret");
		}

		// check client scopes (roles) or fail
		if (!ClientUtil.validateRequestedScopes(clientId, scopes)) {
			return ResponseUtils.responseInvalidScope("invalid client scope(s) requested");
		}

		// check username and password or fail
		if (!checkUserAndPassword(username, password)) {
			return ResponseUtils.responseAccessDenied("invalid username or password");
		}

		// check scopes (roles) for user or fail
		if (!checkScopes(username, scopes)) {
			return ResponseUtils.responseInvalidScope("invalid user scope(s) requested");
		}

		// add token to database
		// TODO
		TokenMapEntry tme = new TokenMapEntry(accessToken,
				LocalDateTime.now().plusSeconds(ACCESS_TOKEN_EXPIRATION_SECONDS), scopes, clientId, username, "realm");
		tokenMap.put(accessToken, tme);
		return null;
	}

	private boolean checkUserAndPassword(String username, String password) {
		// TODO Auto-generated method stub
		return false;
	}

	private static class TokenMapEntry {

		private final String accessToken;
		private final LocalDateTime expiration;
		private final Set<String> scopes;
		private final String clientId;
		private final String username;
		private final String realm;

		public TokenMapEntry(String accessToken, LocalDateTime expiration, Set<String> scopes, String clientId,
				String username, String realm) {
			this.accessToken = accessToken;
			this.expiration = expiration;
			this.scopes = scopes;
			this.clientId = clientId;
			this.username = username;
			this.realm = realm;
		}

		public boolean isValid() {
			return LocalDateTime.now().isBefore(expiration);
		}

		public TokenMapEntry(String accessToken, LocalDateTime expiration, Set<String> scopes, String clientId) {
			this(accessToken, expiration, scopes, clientId, null, null);
		}

	}

}
