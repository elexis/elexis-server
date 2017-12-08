package info.elexis.server.core.scheduler;

import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.Task;

public interface ISchedulerTask {

	public static final SchedulingPattern DAILY_NIGHT = new SchedulingPattern("30 2 * * *");
	public static final SchedulingPattern EVERY_5_MINUTES = new SchedulingPattern("*/5 * * * *");

	/**
	 * The {@link SchedulingPattern} to apply
	 * 
	 * @see <a href=
	 *      "http://www.sauronsoftware.it/projects/cron4j/manual.php#p02">www.
	 *      sauronsoftware.it/projects/cron4j/manual.php#p02</a> Cron4J Scheduling patterns documentation
	 */
	public SchedulingPattern getSchedulingPattern();

	public void setTaskId(String taskId);

	public String getTaskId();

	public Task getTask();
}
