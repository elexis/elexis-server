package info.elexis.server.core.constants;

public class RestPathConstants {

	public static final String BASE_URL_CORE = "/";

	/** GET: halts the system **/
	public static final String HALT = BASE_URL_CORE + "halt";
	public static final String RESTART = BASE_URL_CORE + "restart";
	public static final String SCHEDULER = BASE_URL_CORE + "scheduler";
	public static final String ELEXIS_CONNECTION = BASE_URL_CORE + "elexis/connection";
	public static final String LOGIN = BASE_URL_CORE+"login";
}
