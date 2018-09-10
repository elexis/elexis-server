package info.elexis.server.core.security;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * @see https://www.javatips.net/api/WebAPI-master/src/main/java/org/ohdsi/webapi/shiro/CorsFilter.java
 */
public class CorsFilter extends AdviceFilter {

	@Override
	protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
		// check if it's CORS request
		//
		HttpServletRequest httpRequest = WebUtils.toHttp(request);
		String requestOrigin = httpRequest.getHeader("Origin");
		if (requestOrigin == null) {
			return true;
		}

		// set headers
		//
		HttpServletResponse httpResponse = WebUtils.toHttp(response);
		// TODO limit (?)
		httpResponse.setHeader("Access-Control-Allow-Origin", "*");
		httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

		// stop processing if it's preflight request
		//
		String requestMethod = httpRequest.getHeader("Access-Control-Request-Method");
		String method = httpRequest.getMethod();
		if (requestMethod != null && "OPTIONS".equalsIgnoreCase(method)) {
			httpResponse.setHeader("Access-Control-Allow-Headers",
					"origin, content-type, x-requested-with, accept, authorization, prefer");
			httpResponse.setHeader("Access-Control-Allow-Methods", "GET, HEAD, POST, PUT, DELETE, TRACE, OPTIONS");
			httpResponse.setHeader("Access-Control-Max-Age", "1209600");
			httpResponse.setStatus(HttpServletResponse.SC_OK);

			return false;
		}

		// continue processing request
		//
		httpResponse.setHeader("Access-Control-Expose-Headers", "Bearer");
		return true;
	}

}
