package info.elexis.server.core.p2;

import org.eclipse.core.runtime.IStatus;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import info.elexis.server.core.p2.internal.ProvisioningHelper;
import info.elexis.server.core.scheduler.AbstractSchedulerTask;
import info.elexis.server.core.scheduler.ISchedulerService;
import it.sauronsoftware.cron4j.SchedulingPattern;
import it.sauronsoftware.cron4j.TaskExecutionContext;

//@Component(immediate = true)
public class SystemUpdateTask extends AbstractSchedulerTask {
	
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
		context.setStatusMessage("Checking for updates");
		IStatus updateAllFeatures = ProvisioningHelper.updateAllFeatures();
		context.setStatusMessage(updateAllFeatures.getMessage());
	}

	@Override
	public boolean supportsStatusTracking() {
		return true;
	}

	@Override
	public SchedulingPattern getSchedulingPattern() {
		return DAILY_NIGHT;
	}
}
