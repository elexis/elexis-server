package info.elexis.server.core.security;

import java.io.IOException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.AuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * If the subject is not authenticated, this filter simply returns 403 instead
 * of redirecting the user to a login page.
 */
public class NoLoginRedirectShiroFilter extends AuthorizationFilter {

	private static final String message = "Access denied.";

	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		Subject subject = getSubject(request, response);
		// TODO remove
		System.out.println(subject.getPrincipal());
		return subject.isAuthenticated();
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
		HttpServletResponse httpResponse;
		try {
			httpResponse = WebUtils.toHttp(response);
		} catch (ClassCastException ex) {
			// Not a HTTP Servlet operation
			return super.onAccessDenied(request, response);
		}
		if (message == null)
			httpResponse.sendError(403);
		else
			httpResponse.sendError(403, message);
		return false; // No further processing.
	}
}