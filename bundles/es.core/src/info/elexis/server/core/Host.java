//package info.elexis.server.core;
//
//import java.net.InetAddress;
//import java.net.UnknownHostException;
//
//import org.apache.commons.lang3.StringUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class Host {
//
//	private static Logger log;
//
//	public static final String HOSTNAME;
//	public static final int HTTP_PORT;
//	public static final String BASE_URL;
//
//	static {
//		log = LoggerFactory.getLogger(Host.class);
//
//		InetAddress myHost = null;
//		try {
//			myHost = InetAddress.getLocalHost();
//		} catch (UnknownHostException e) {
//			log.error("Could not resolve hostname", e);
//		}
//
//		HOSTNAME = (myHost != null) ? myHost.getCanonicalHostName() : null;
//
//		String httpPort = System.getProperty("jetty.http.port");
//		if (StringUtils.isNumeric(httpPort)) {
//			HTTP_PORT = Integer.parseInt(httpPort);
//		} else {
//			HTTP_PORT = 8380;
//		}
//
//		if (HTTP_PORT != 80) {
//			BASE_URL = "http://" + HOSTNAME + ":" + HTTP_PORT + "/";
//		} else {
//			BASE_URL = "http://" + HOSTNAME + "/";
//		}
//
//		log.info("Hostname is [{}]", HOSTNAME);
//	}
//
//	public static String getLocalhostBaseUrl() {
//		return "http://localhost:" + HTTP_PORT + "/";
//	}
//
//}
