package info.elexis.server.core;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

public class SystemPropertyConstants {
	
	private SystemPropertyConstants(){}
	
	/**
	 * The ID of the station running this instance. If not set, defaults to
	 * {@value #STATION_ID_DEFAULT}. Use {@link #getStationId()} to retrieve the resolved value
	 */
	public static final String STATION_ID = "stationId";
	/**
	 * @see #STATION_ID
	 */
	public static final String STATION_ID_DEFAULT = "ELEXIS-SERVER";
	
	public static final String FILTER_NAME = System.getenv("foo");

	/**
	 * Disable the security filters for all resources. Allows <b>unprotected access</b> to
	 * resources.
	 * <ul>
	 * <li>/fhir</li>
	 * <li>/services</li>
	 * </ul>
	 */
	public static final String DISABLE_WEB_SECURITY = "disable.web.security";
	
	private static final boolean isDisableWebSecurity =
		Boolean.valueOf(System.getProperty(DISABLE_WEB_SECURITY)) ? setValueAndLog(true) : false;
	
	public static final boolean isDisableWebSecurity() {
		return isDisableWebSecurity;
	}
	
	private static boolean setValueAndLog(boolean value){
		LoggerFactory.getLogger(SystemPropertyConstants.class).error(
			"!!!!! Web security is administratively disabled. System is unprotected. Remove [{}] system property !!!!!",
			DISABLE_WEB_SECURITY);
		return true;
	}
	
	public static String getStationId(){
		String stationId = System.getProperty(STATION_ID);
		return (StringUtils.isNotBlank(stationId)) ? stationId : STATION_ID_DEFAULT;
	}
	
}
