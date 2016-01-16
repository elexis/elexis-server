package info.elexis.server.core.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.Task;

public enum Scheduler {
	INSTANCE;

	public static final SchedulingPattern DAILY_NIGHT = new SchedulingPattern("30 2 * * *");
	
	private Logger log = LoggerFactory.getLogger(Scheduler.class);

	private it.sauronsoftware.cron4j.Scheduler scheduler;
	private SchedulerStatus ss = new SchedulerStatus();
	private SchedulerListenerImpl sli;

	private Scheduler() {
		scheduler = new it.sauronsoftware.cron4j.Scheduler();
		sli = new SchedulerListenerImpl(ss);
		scheduler.addSchedulerListener(sli);
	}

	public void startScheduler() {
		log.debug("Starting scheduler");
		scheduler.start();
	}

	public void stopScheduler() {
		log.debug("Stopping scheduler");
		scheduler.stop();
	}
	
	public String schedule(SchedulingPattern schedulingPattern, Task task) {
		String id = scheduler.schedule(schedulingPattern, task);
		ss.register(task, schedulingPattern, id);
		return id;
	}
	
	public void deschedule(String taskId) {
		scheduler.deschedule(taskId);
	}

	public SchedulerStatus getSchedulerStatus() {
		return ss;
	}
	
}
