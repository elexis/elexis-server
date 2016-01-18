package info.elexis.server.core.scheduler;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(name = "SchedulerService")
public class SchedulerService implements ISchedulerService {

	private Logger log = LoggerFactory.getLogger(SchedulerService.class);
	
	private it.sauronsoftware.cron4j.Scheduler scheduler;
	private static SchedulerStatus ss = new SchedulerStatus();
	private SchedulerListenerImpl sli;
	
	@Override
	public void schedule(ISchedulerTask ast) {
		log.debug("Adding scheduler task "+ast.getClass().getName());
		String id = scheduler.schedule(ast.getSchedulingPattern(), ast.getTask());
		ss.register(ast.getTask(), ast.getSchedulingPattern(), id);
		ast.setTaskId(id);
	}

	@Override
	public void deschedule(ISchedulerTask ast) {
		log.debug("Removing scheduler task "+ast.getClass().getName());
		String taskId = ast.getTaskId();
		ss.deregister(taskId);
		scheduler.deschedule(taskId);
	}
	
	@Activate
	public void activate() {
		log.debug("Starting scheduler service");
		scheduler = new it.sauronsoftware.cron4j.Scheduler();
		sli = new SchedulerListenerImpl(ss);
		scheduler.addSchedulerListener(sli);
		scheduler.start();
	}
	
	@Deactivate
	public void deactivate() {
		log.debug("Stopping scheduler service");
		scheduler.stop();
		scheduler.removeSchedulerListener(sli);
		scheduler = null;
		sli = null;
	}

	public static SchedulerStatus getSchedulerStatus() {
		return ss;
	}

}
