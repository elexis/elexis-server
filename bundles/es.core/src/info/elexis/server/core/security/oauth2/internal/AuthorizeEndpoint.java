package info.elexis.server.core.security.oauth2.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.issuer.UUIDValueGenerator;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.apache.oltu.oauth2.common.message.types.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthorizeEndpoint extends HttpServlet {

	private static final long serialVersionUID = 6604481686591578744L;

	private Logger log = LoggerFactory.getLogger(AuthorizeEndpoint.class);

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		try {
			OAuthAuthzRequest oAuthRequest = new OAuthAuthzRequest(request);

			// determine response type
			ResponseType responseType = null;
			try {
				responseType = ResponseType.valueOf(oAuthRequest.getResponseType().toUpperCase());

			} catch (IllegalArgumentException iae) {
				log.error("Invalid response type [{}]", oAuthRequest.getResponseType());
				ResponseUtils.processResponse(response, null,
						ResponseUtils.responseInvalidRequest("invalid response type"));
				return;
			}
			// SMART-on-FHIR fixed to CODE
			if (!(ResponseType.CODE == responseType)) {
				log.warn("unsupported response type requested [{}]", oAuthRequest.getResponseType());
				ResponseUtils.processResponse(response, null,
						ResponseUtils.responseInvalidRequest("unsupported response type"));
				return;
			}

			valiadateClientId();
			validateRedirectionURI(oAuthRequest);
			
			askUserToValidateRequest();

			OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new UUIDValueGenerator());

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

			builder.location(oAuthRequest.getParam(OAuth.OAUTH_REDIRECT_URI));

			OAuthResponse buildQueryMessage = builder.buildQueryMessage();
			response.sendRedirect(buildQueryMessage.getLocationUri());

		} catch (OAuthProblemException e) {
			String redirectUri = e.getRedirectUri();

			try {
				OAuthResponse res = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND).error(e)
						.location(redirectUri).buildQueryMessage();
				response.sendRedirect(res.getLocationUri());
			} catch (OAuthSystemException e1) {
				e1.printStackTrace();
			}

		} catch (OAuthSystemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void validateRedirectionURI(OAuthAuthzRequest oAuthRequest) {
		// TODO Auto-generated method stub
		
	}

	private void askUserToValidateRequest() {
		// TODO Auto-generated method stub
		
	}

	private void valiadateClientId() {
		// TODO Auto-generated method stub
		
	}

//	private void validateRedirectionURI(OAuthAuthzRequest oAuthRequest, Client client) throws OAuthProblemException) {
//		// https or localhost only
//		// must match one of the clients pre-registered redirect URIs
//		  String redirectURI = oAuthRequest.getRedirectURI();
//	        if (!client.getRedirectUri().equals(redirectURI)) {
//	            log.error("redirect_uri : {}", redirectURI);
//	            throw OAuthProblemException.error("invalid_request");
//	        }
//	}

}
