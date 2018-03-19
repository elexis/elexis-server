package info.elexis.server.core.security.internal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Dictionary;

import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.shiro.web.servlet.IniShiroFilter;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.eclipse.equinox.http.servlet.ExtendedHttpService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.publisher.ServletConfiguration;

/**
 * Register the {@link ShiroFilter} with OSGI Jax RS in order to enforce our
 * security requirements
 */
@Component(service = ServletConfiguration.class)
public class JaxRsServletConfiguration implements ServletConfiguration {

	private Logger log = LoggerFactory.getLogger(JaxRsServletConfiguration.class);

	@Override
	public HttpContext getHttpContext(HttpService httpService, String rootPath) {
		Thread.currentThread().setContextClassLoader(JaxRsServletConfiguration.class.getClassLoader());
		ExtendedHttpService extHttpService = (ExtendedHttpService) httpService;
		try {
			// https://issues.apache.org/jira/browse/SHIRO-617?filter=-2
			String config = IOUtils.toString(this.getClass().getResourceAsStream("shiro-jaxrs.ini"),
					Charset.forName("UTF-8"));
			IniShiroFilter iniShiroFilter = new IniShiroFilter();
			iniShiroFilter.setConfig(config);
			// TODO fetch configured root path
			extHttpService.registerFilter("/services", iniShiroFilter, getInitParams(extHttpService, rootPath), null);
			log.info("Registered IniShiroFilter filter for /services");
		} catch (ServletException | NamespaceException | IOException e) {
			log.error("Error registering shiro filter", e);
		}
		return null;
	}

	@Override
	public Dictionary<String, String> getInitParams(HttpService httpService, String rootPath) {
		return null;
	}

}