package info.elexis.server.core.security.oauth2.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;

public class AuthzEndpoint extends HttpServlet {

	private static final long serialVersionUID = 6604481686591578744L;
	
	public static final String ENDPOINT = "/login/oauth/authorize";

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException, IOException {
		OAuthAuthzRequest oauthRequest = null;

		OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());

		try {
			oauthRequest = new OAuthAuthzRequest(request);

			// build response according to response_type
			String responseType = oauthRequest.getParam(OAuth.OAUTH_RESPONSE_TYPE);

			OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse.authorizationResponse(request,
					HttpServletResponse.SC_FOUND);

			if (responseType.equals(ResponseType.CODE.toString())) {
				builder.setCode(oauthIssuerImpl.authorizationCode());
			}
			if (responseType.equals(ResponseType.TOKEN.toString())) {
				builder.setAccessToken(oauthIssuerImpl.accessToken());
				builder.setTokenType(OAuth.DEFAULT_TOKEN_TYPE.toString());
				builder.setExpiresIn(3600l);
			}

			builder.location(oauthRequest.getParam(OAuth.OAUTH_REDIRECT_URI));

			OAuthResponse buildQueryMessage = builder.buildQueryMessage();
			resp.sendRedirect(buildQueryMessage.getLocationUri());

		} catch (OAuthProblemException e) {
			String redirectUri = e.getRedirectUri();

			try {
				OAuthResponse response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
						.location(redirectUri).buildQueryMessage();
				resp.sendRedirect(response.getLocationUri());
			} catch (OAuthSystemException e1) {
				e1.printStackTrace();
			}

		} catch (OAuthSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
