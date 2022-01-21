package info.elexis.server.core.redmine.internal;

public class Constants {
	
	
	/**
	 * Expected environment variable containing the api key to use for the redmine connection
	 */
	public static final String ENV_VAR_REDMINE_API_KEY = "REDMINE_APIKEY";
	
	/**
	 * Optional environment variable containing the redmine base url to use, defaults to
	 * <code>https://redmine.medelexis.ch</code>
	 */
	public static final String ENV_VAR_REDMINE_BASE_URL = "REDMINE_BASEURL";
	
	public static final String DEFAULT_REDMINE_BASE_URL = "https://redmine.medelexis.ch";
	public static final String DEFAULT_REDMINE_PROJECT = "qfeedback3";
	
	/**
	 * Environment variable containing the users apiKey for the Medelexis Management Information
	 * System (MIS)
	 */
	public static final String ENV_VAR_MIS_API_KEY = "MIS_APIKEY";
	
	/**
	 * The numeric Id of the mis project
	 */
	public static final String ENV_VAR_MIS_PROJECTID = "MIS_PROJECTID";

	/**
	 * The Elexis branch of this installation (added via DockerFile during build)
	 */
	public static final String ENV_VAR_ELEXIS_BRANCH = "ELEXIS-BRANCH";
}
