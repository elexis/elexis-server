package info.elexis.server.core.p2;

import org.eclipse.core.runtime.IStatus;

import info.elexis.server.core.p2.internal.ProvisioningHelper;
import it.sauronsoftware.cron4j.Task;
import it.sauronsoftware.cron4j.TaskExecutionContext;

public class SystemUpdateTask extends Task {
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
}
