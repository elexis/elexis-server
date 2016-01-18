package info.elexis.server.core.scheduler;

import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.Task;

public interface ISchedulerTask {

	public static final SchedulingPattern DAILY_NIGHT = new SchedulingPattern("30 2 * * *");

	public SchedulingPattern getSchedulingPattern();

	public void setTaskId(String taskId);

	public String getTaskId();

	public Task getTask();
}
