package info.elexis.server.core.extension;

import javax.ws.rs.core.Response;

import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.Task;

@Deprecated
public interface IESPlugin {
	
	public String geti18nPluginDescription();
	
	public String getRESTSubResourceId();
	
	public Response getRESTResponseForQuery(String queryString);
	
	/**
	 * 
	 * @return <code>null</code> if this plugin does not require scheduled
	 *         execution
	 */
	public Task getScheduledTask();

	public SchedulingPattern getDefaultSchedulingPattern();
}
