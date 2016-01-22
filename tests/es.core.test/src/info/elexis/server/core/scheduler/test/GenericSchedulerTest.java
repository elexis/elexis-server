package info.elexis.server.core.scheduler.test;

import java.util.Date;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.scheduler.AbstractSchedulerTask;
import info.elexis.server.core.scheduler.ISchedulerService;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskExecutionContext;

@Component(immediate = true)
public class GenericSchedulerTest extends AbstractSchedulerTask {
	
	private static Logger log = LoggerFactory.getLogger(GenericSchedulerTest.class);
	
	@Reference(service = ISchedulerService.class, 
			   cardinality = ReferenceCardinality.MANDATORY, 
			   policy = ReferencePolicy.STATIC,
			   name = "SchedulerService")
	public void bind(ISchedulerService iss) {
		iss.schedule(this);
	}
	
	public void unbind(ISchedulerService iss) {
		iss.deschedule(this);
	}
	
	@Override
	public void execute(TaskExecutionContext context) throws RuntimeException {
		context.setStatusMessage("Executing generic scheduler test");
		log.info("Executing scheduler task "+new Date());
		context.setStatusMessage("done");
		context.setCompleteness(1);
	}

	@Override
	public boolean supportsStatusTracking() {
		return true;
	}

	@Override
	public SchedulingPattern getSchedulingPattern() {
		// every minute pattern
		return new SchedulingPattern("* * * * *");
	}
}