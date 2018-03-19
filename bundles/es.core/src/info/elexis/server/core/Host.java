package info.elexis.server.core;

/**
 * TODO refactor - deprecate ?
 */
public class Host {

	public static String getHostname() {
		String hostName = "localhost";
		// try {
		// hostName = InetAddress.getLocalHost().getHostName();
		// } catch (UnknownHostException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		return hostName;
	}

	public static String getBaseUrlSecure() {
		return "https://" + getHostname() + ":" + getSecureHttpPort() + "/";
	}

	public static String getBaseUrl() {
		return "http://" + getHostname() + ":" + getHttpPort() + "/";
	}

	public static String getHttpPort() {
		String port = System.getProperty("jetty.http.port");
		if (port == null) {
			port = "8380";
		}
		return port;
	}

	public static String getSecureHttpPort() {
		String port = System.getProperty("jetty.ssl.port");
		if (port == null) {
			port = "8480";
		}
		return port;
	}

	public static String getOpenIDBaseUrl() {
		return getBaseUrl() + "openid/";
	}

	public static String getOpenIDBaseUrlSecure() {
		return getBaseUrlSecure() + "openid/";
	}

	public static String getLocalhostBaseUrl() {
		return "http://localhost:" + getHttpPort() + "/";
	}

}
