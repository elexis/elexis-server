package info.elexis.server.core.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Optional;

import javax.ws.rs.container.ContainerRequestContext;

import org.apache.shiro.subject.Subject;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.provider.security.AuthenticationHandler;
import com.eclipsesource.jaxrs.provider.security.AuthorizationHandler;

@Component(immediate = true)
public class HTTPAuthHandler implements AuthenticationHandler, AuthorizationHandler {

	private Logger log = LoggerFactory.getLogger(HTTPAuthHandler.class);
	
	private String HEADER_SESSION_ID = "sessionId";

	@Override
	public boolean isUserInRole(Principal user, String role) {
		Optional<Subject> subj = ShiroSecurityService.getSubjectBySessionId(user.getName());
		if (subj.isPresent()) {
			Subject subject = subj.get();
			if (subject.hasRole("admin") || subject.hasRole(role)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Principal authenticate(ContainerRequestContext rc) {
		String sHost = rc.getUriInfo().getBaseUri().getHost();
		log.trace("[" + rc.getRequest().getMethod() + "] S " + sHost + " D " + rc.getUriInfo().getPath());
		
		String sessionId = rc.getHeaderString(HEADER_SESSION_ID);
		if (sessionId != null) {
			return new PrincipalImpl(sessionId);
		}
		
		// support for BasicAuth - should we??
//		
//		String authHeader = rc.getHeaderString("Authorization");
//	    if (authHeader != null && authHeader.startsWith("Basic ")) {
//	        String[] up = parseBasicAuthHeader(authHeader.substring(authHeader.indexOf(" ") + 1));
//	        String username = up[0];
//	        String password = up[1];
//	        Optional<Serializable> oSessId = HTTPAuthHandler.getSessionId(username, password);
//	        if(oSessId.isPresent()) {
//	        	String sessId = (String) oSessId.get();
//	        	rc.getHeaders().add(HEADER_SESSION_ID, sessId);
//	        	return new PrincipalImpl(sessId);
//	        }
//	    }
//	    
//	    Response resp = Response.status(Response.Status.UNAUTHORIZED).build();
//	    resp.getHeaders().add("WWW-Authenticate", "BASIC realm=\"SecureFiles\"");
//	    rc.abortWith(resp);
	    
	    //

	    return null;
	}

//	private String[] parseBasicAuthHeader(String enc) {
//	    byte[] bytes = Base64.decodeBase64(enc.getBytes());
//	    String s = new String(bytes);
//	    int pos = s.indexOf( ":" );
//	    if( pos >= 0 )
//	        return new String[] { s.substring( 0, pos ), s.substring( pos + 1 ) };
//	    else
//	        return new String[] { s, null };
//	}
	
	@Override
	public String getAuthenticationScheme() {
		return null;
	}

	public static Optional<Serializable> getSessionId(String userId, String password) {
		return ShiroSecurityService.authenticate(userId, password.toCharArray());
	}
}
