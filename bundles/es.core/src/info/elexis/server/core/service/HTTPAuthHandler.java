package info.elexis.server.core.service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;

import javax.ws.rs.container.ContainerRequestContext;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.provider.security.AuthenticationHandler;
import com.eclipsesource.jaxrs.provider.security.AuthorizationHandler;

import info.elexis.server.core.security.AccessControl;
import info.elexis.server.core.security.User;

@Component(immediate = true)
public class HTTPAuthHandler implements AuthenticationHandler, AuthorizationHandler {

	private Logger log = LoggerFactory.getLogger(HTTPAuthHandler.class);

	@Override
	public boolean isUserInRole(Principal user, String role) {
		return AccessControl.request(user, role);
	}

	@Override
	public Principal authenticate(ContainerRequestContext rc) {
		String sHost = rc.getUriInfo().getBaseUri().getHost();
		log.trace("[" + rc.getRequest().getMethod() + "] S " + sHost + " D " + rc.getUriInfo().getPath());

		try {
			InetAddress sHostIA = InetAddress.getByName(sHost);
			if (sHostIA.isAnyLocalAddress() || sHostIA.isLoopbackAddress()) {
				// requests from localhost are granted ADMIN rights
				return User.ADMIN;
			}
		} catch (UnknownHostException e) {
			// definitely not localhost
		}

		String user = rc.getHeaderString("user");
		if (user == null) {
			return null;
		}
		return new User(user);
	}

	@Override
	public String getAuthenticationScheme() {
		return null;
	}
}
