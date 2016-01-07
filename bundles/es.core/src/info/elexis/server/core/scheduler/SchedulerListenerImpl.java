package info.elexis.server.core.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.sauronsoftware.cron4j.SchedulerListener;
import it.sauronsoftware.cron4j.TaskExecutor;

import static info.elexis.server.core.scheduler.SchedulerStatus.*;

public class SchedulerListenerImpl implements SchedulerListener {

	private Logger log = LoggerFactory.getLogger(SchedulerListenerImpl.class);
	private SchedulerStatus ss;

	public SchedulerListenerImpl(SchedulerStatus ss) {
		this.ss = ss;
	}

	@Override
	public void taskLaunching(TaskExecutor executor) {
		ss.updateExecution(STATE_LAUNCH, executor.getTask(), executor);
		log.info("LAUNCH " + toString(executor));
	}

	@Override
	public void taskSucceeded(TaskExecutor executor) {
		ss.updateExecution(STATE_SUCCESS, executor.getTask(), executor);
		log.info("SUCCESS " + toString(executor));
	}

	@Override
	public void taskFailed(TaskExecutor executor, Throwable exception) {
		ss.updateExecution(STATE_FAIL, executor.getTask(), executor);
		log.warn("FAIL " + toString(executor));
	}

	private String toString(TaskExecutor executor) {
		return executor.getTask().getClass().getName();
	}
}
