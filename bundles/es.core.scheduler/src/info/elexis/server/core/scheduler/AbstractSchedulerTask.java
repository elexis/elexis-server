package info.elexis.server.core.scheduler;

import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.Task;

public abstract class AbstractSchedulerTask extends Task implements ISchedulerTask {
	
	String taskId;

	public abstract SchedulingPattern getSchedulingPattern();

	@Override
	public void setTaskId(String taskId) {
		this.taskId = taskId;
	};

	@Override
	public String getTaskId() {
		return this.taskId;
	}
	
	@Override
	public Task getTask() {
		return this;
	}
}
