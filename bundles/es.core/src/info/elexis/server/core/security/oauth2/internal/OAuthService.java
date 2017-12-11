package info.elexis.server.core.security.oauth2.internal;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.oltu.oauth2.common.message.OAuthResponse.OAuthResponseBuilder;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.security.oauth2.ClientUtil;

public class OAuthService {

	private static final long ACCESS_TOKEN_EXPIRATION_SECONDS = 3600;

	private static final Map<String, TokenMapEntry> accessTokenMap = Collections
			.synchronizedMap(new PassiveExpiringMap<String, TokenMapEntry>(ACCESS_TOKEN_EXPIRATION_SECONDS * 1000));

	private Logger log = LoggerFactory.getLogger(OAuthService.class);

	public boolean refreshTokenSupported() {
		return false;
	}

	public long getExpireIn(String accessToken) {
		return ACCESS_TOKEN_EXPIRATION_SECONDS;
	}

	public boolean checkAccessToken(String accessToken, HttpServletRequest httpServletRequest) {
		if (accessTokenMap.containsKey(accessToken)) {
			TokenMapEntry tokenMapEntry = accessTokenMap.get(accessToken);
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
				LocalDateTime.now().plusSeconds(ACCESS_TOKEN_EXPIRATION_SECONDS), scopes, clientId, null);
		accessTokenMap.put(accessToken, tme);

		return null;
	}

	/**
	 * Retrieve the scopes for the provided access token
	 * 
	 * @param principals
	 * @return
	 */
	public Set<String> getScopes(String accessToken) {
		boolean containsKey = accessTokenMap.containsKey(accessToken);
		if (containsKey) {
			return accessTokenMap.get(accessToken).getScopes();
		}
		return Collections.emptySet();
	}

	public OAuthResponseBuilder addAcessTokenResourceOwnerPasswordClientCredentials(String accessToken, String username,
			String password, String clientId, String clientSecret, Set<String> scopes) {
		// check client or fail
		if (!ClientUtil.validateClient(clientId, clientSecret)) {
			return ResponseUtils.responseUnauthClient("invalid client id or client secret");
		}

		// check username and password and scopes (roles) or fail
		if (!checkUserAndPasswordAndScopes(username, password, scopes)) {
			return ResponseUtils.responseAccessDenied("invalid username, password or scopes");
		}

		// add token to database
		// TODO
		TokenMapEntry tme = new TokenMapEntry(accessToken,
				LocalDateTime.now().plusSeconds(ACCESS_TOKEN_EXPIRATION_SECONDS), scopes, clientId, username);
		accessTokenMap.put(accessToken, tme);
		return null;
	}

	private boolean checkUserAndPasswordAndScopes(String userId, String password, Set<String> scopes) {
		try {
			Subject subject = SecurityUtils.getSubject();
			UsernamePasswordToken upt = new UsernamePasswordToken(userId, password);
			subject.login(upt);
			subject.hasAllRoles(scopes);
			subject.logout();
			return true;
		} catch (AuthenticationException ae) {
			log.warn("User [{}] authentication error: {}", userId, ae.getMessage());
		}
		return false;
	}

	private static class TokenMapEntry {

		private final String accessToken;
		private final LocalDateTime expiration;
		private final Set<String> scopes;
		private final String clientId;
		private final String username;

		public TokenMapEntry(String accessToken, LocalDateTime expiration, Set<String> scopes, String clientId,
				String username) {
			this.accessToken = accessToken;
			this.expiration = expiration;
			this.scopes = scopes;
			this.clientId = clientId;
			this.username = username;
		}

		public Set<String> getScopes() {
			return scopes;
		}

		public boolean isValid() {
			return LocalDateTime.now().isBefore(expiration);
		}

		@Override
		public String toString() {
			return String.format("[%36s]", accessToken) + " " + expiration + " " + clientId + " " + username + " "
					+ " (" + scopes + ")";
		}
	}

	public static String printStatus() {
		StringBuilder sb = new StringBuilder();
		Collection<TokenMapEntry> values = accessTokenMap.values();
		for (TokenMapEntry tokenMapEntry : values) {
			sb.append(tokenMapEntry + "\n");
		}
		return sb.toString();
	}

}
