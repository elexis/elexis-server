package info.elexis.server.core.p2;

import java.util.Collection;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.p2.operations.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.p2.internal.Provisioner;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

public class SystemUpdateTask extends Task {

	private Logger log = LoggerFactory.getLogger(SystemUpdateTask.class);
	
	@Override
	public void execute(TaskExecutionContext context) throws RuntimeException {
		Provisioner p = new Provisioner();
		if(!p.updateServiceIsConnectable()) {
			log.warn("Update service is not connectable.");
			context.setStatusMessage("Update service is not connectable.");
			return;
		}
		
		Collection<Update> availableUpdates = p.getAvailableUpdates();
		if(availableUpdates.size()>0) {
			p.update(availableUpdates, new NullProgressMonitor());
		}
	}

	@Override
	public boolean supportsStatusTracking() {
		return true;
	}
}
