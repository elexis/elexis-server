package info.elexis.server.core.connector.elexis.instances;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.common.InstanceStatus;

public class InstanceService {

	private static Map<String, ServerHeldInstanceStatus> instanceStatusMap = new HashMap<String, ServerHeldInstanceStatus>();

	public synchronized static IStatus updateInstanceStatus(InstanceStatus inst, String remoteAddress) {
		ServerHeldInstanceStatus shis;
		if (instanceStatusMap.containsKey(inst.getUuid())) {
			shis = instanceStatusMap.get(inst.getUuid());
		} else {
			shis = new ServerHeldInstanceStatus();
			instanceStatusMap.put(inst.getUuid(), shis);
		}
		shis.setInstanceStatus(inst);
		shis.setRemoteAddress(remoteAddress);
		shis.setLastUpdate(LocalDateTime.now());

		return Status.OK_STATUS;
	}

	public static List<ServerHeldInstanceStatus> getInstanceStatus() {
		return new ArrayList<ServerHeldInstanceStatus>(instanceStatusMap.values());
	}

}
