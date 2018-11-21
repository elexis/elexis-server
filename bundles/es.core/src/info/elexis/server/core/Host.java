package info.elexis.server.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Host {

	private static Logger log;

	public static final String HOSTNAME;
	public static final int HTTP_PORT;
	public static final int HTTPS_PORT;
	public static final String BASE_URL;
	public static final String BASE_URL_SECURE;

	static {
		log = LoggerFactory.getLogger(Host.class);

		InetAddress myHost = null;
		try {
			myHost = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			log.error("Could not resolve hostname", e);
		}

		HOSTNAME = (myHost != null) ? myHost.getCanonicalHostName() : null;

		String httpPort = System.getProperty("jetty.http.port");
		if (StringUtils.isNumeric(httpPort)) {
			HTTP_PORT = Integer.parseInt(httpPort);
		} else {
			HTTP_PORT = 8380;
		}

		if (HTTP_PORT != 80) {
			BASE_URL = "http://" + HOSTNAME + ":" + HTTP_PORT + "/";
		} else {
			BASE_URL = "http://" + HOSTNAME + "/";
		}

		String httpsPort = System.getProperty("jetty.ssl.port");
		if (StringUtils.isNumeric(httpsPort)) {
			HTTPS_PORT = Integer.parseInt(httpsPort);
		} else {
			HTTPS_PORT = 8480;
		}

		if (HTTPS_PORT != 443) {
			BASE_URL_SECURE = "https://" + HOSTNAME + ":" + HTTPS_PORT + "/";
		} else {
			BASE_URL_SECURE = "https://" + HOSTNAME + "/";
		}

		log.info("Hostname is [{}]", HOSTNAME);
	}

	public static String getOpenIDBaseUrl() {
		return BASE_URL + "openid/";
	}

	public static String getOpenIDBaseUrlSecure() {
		return BASE_URL_SECURE + "openid/";
	}

	public static String getLocalhostBaseUrl() {
		return "http://localhost:" + HTTP_PORT + "/";
	}

}
