package info.elexis.server.core.scheduler;

import org.eclipse.core.runtime.IProgressMonitor;

import it.sauronsoftware.cron4j.TaskExecutionContext;

public class SchedulerProgressMonitor implements IProgressMonitor {

	private TaskExecutionContext context;
	private double stepSize;
	private double workDone;
	private String taskName;

	public SchedulerProgressMonitor(TaskExecutionContext context) {
		this.context = context;
	}
	
	@Override
	public void beginTask(String name, int totalWork) {
		this.taskName = name;
		context.setStatusMessage("Starting task "+name);
		this.workDone = 0;
		this.stepSize = (100/totalWork);
		context.setCompleteness(0);
	}

	@Override
	public void done() {
		context.setCompleteness(1);
	}

	@Override
	public void internalWorked(double work) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setCanceled(boolean value) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setTaskName(String name) {
		this.taskName = name;
	}

	@Override
	public void subTask(String name) {
		context.setStatusMessage(taskName+": "+name);
	}

	@Override
	public void worked(int work) {
		workDone += (stepSize*work);
		context.setCompleteness(workDone);
	}

}
