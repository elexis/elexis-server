package info.elexis.server.core.redmine.internal;

public class Constants {
	
	/**
	 * Expected environment variable containing the api key to use for the redmine connection
	 */
	public static final String ENV_VAR_REDMINE_API_KEY = "redmine.apikey";
	
	/**
	 * Optional environment variable containing the redmine base url to use, defaults to
	 * <code>https://redmine.medelexis.ch</code>
	 */
	public static final String ENV_VAR_REDMINE_BASE_URL = "redmine.baseurl";
	
	
	
	public static final String DEFAULT_REDMINE_BASE_URL = "https://redmine.medelexis.ch";
	public static final String DEFAULT_REDMINE_PROJECT = "qfeedback3";
	
}
