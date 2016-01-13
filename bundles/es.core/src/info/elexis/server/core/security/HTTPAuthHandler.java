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

		String sessionId = rc.getHeaderString("sessionId");
		if (sessionId == null) {
			return null;
		}

		return new PrincipalImpl(sessionId);
	}

	@Override
	public String getAuthenticationScheme() {
		return null;
	}

	public static Serializable getSessionId(String userId, String password) {
		return ShiroSecurityService.authenticate(userId, password.toCharArray());
	}
}
