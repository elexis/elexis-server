package info.elexis.server.core.security.oauth2.internal;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.issuer.UUIDValueGenerator;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.OAuthResponse.OAuthErrorResponseBuilder;
import org.apache.oltu.oauth2.common.message.OAuthResponse.OAuthResponseBuilder;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import org.apache.oltu.oauth2.common.utils.OAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OAuth2 Token Endpoint
 */
public class TokenEndpoint extends HttpServlet {

	private static final long serialVersionUID = 7577335233265868814L;

	private Logger log = LoggerFactory.getLogger(TokenEndpoint.class);
	private OAuthService oAuthService = new OAuthService();

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		try {
			if (!request.isSecure()) {
				// do not allow non-ssl connections
				OAuthErrorResponseBuilder error = OAuthResponse.errorResponse(HttpServletResponse.SC_FORBIDDEN)
						.setError(OAuthError.CodeResponse.INVALID_REQUEST).setErrorDescription("SSL required");
				ResponseUtils.processResponse(response, null, error);
				return;
			}

			handlePost(request, response);
		} catch (OAuthSystemException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
	}

	private boolean handlePost(HttpServletRequest request, HttpServletResponse response)
			throws OAuthSystemException, IOException {

		try {
			OAuthTokenRequest oAuthRequest = new OAuthTokenRequest(request);
			String clientId = oAuthRequest.getClientId();
			String refreshToken = oAuthRequest.getRefreshToken();
			Set<String> scopes = oAuthRequest.getScopes();

			// determine grant type, validate client id
			GrantType grantType = null;
			try {
				grantType = GrantType.valueOf(oAuthRequest.getGrantType().toUpperCase());
			} catch (IllegalArgumentException iae) {
				log.error("Invalid grant type [{}]", oAuthRequest.getGrantType());
				return ResponseUtils.processResponse(response, null,
						ResponseUtils.responseInvalidRequest("invalid grant type"));
			}

			// handle grant type request
			OAuthIssuerImpl oAuthIssuer = new OAuthIssuerImpl(new UUIDValueGenerator());
			String accessToken = oAuthIssuer.accessToken();

			switch (grantType) {
			case CLIENT_CREDENTIALS:
				// Client Credentials Flow
				OAuthResponseBuilder error = oAuthService.addAcessTokenClientCredentials(accessToken, clientId,
						oAuthRequest.getClientSecret(), scopes);
				if (error != null) {
					return ResponseUtils.processResponse(response, null, error);
				}
				break;

			case PASSWORD:
				// Resource Owner Password Credentials Flow
				String username = oAuthRequest.getUsername();
				String password = oAuthRequest.getPassword();
				error = oAuthService.addAcessTokenResourceOwnerPasswordClientCredentials(accessToken, username,
						password, clientId, oAuthRequest.getClientSecret(), scopes);
				if (error != null) {
					return ResponseUtils.processResponse(response, null, error);
				}
				break;

			default:
				log.warn("unsupported grant type requested [{}]", oAuthRequest.getGrantType());
				return ResponseUtils.processResponse(response, null,
						ResponseUtils.responseInvalidRequest("unsupported grant type"));
			}

			// generate page content
			String expireIn = String.valueOf(oAuthService.getExpireIn(accessToken));
			return ResponseUtils.processResponse(response, null,
					OAuthASResponse.tokenResponse(HttpServletResponse.SC_OK).setExpiresIn(expireIn)
							.setAccessToken(accessToken).setRefreshToken(refreshToken));

		} catch (OAuthProblemException ex) {
			if (OAuthUtils.isEmpty(ex.getError())) {
				return ResponseUtils.processResponse(response, null,
						ResponseUtils.responseInvalidRequest(ex.getDescription()));
			} else {
				return ResponseUtils.processResponse(response, null, ResponseUtils.responseBadRequest(ex));
			}
		}

	}

}
