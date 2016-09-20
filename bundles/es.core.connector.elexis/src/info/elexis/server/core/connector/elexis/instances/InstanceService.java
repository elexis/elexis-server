package info.elexis.server.core.connector.elexis.instances;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.common.InstanceStatus.STATE;

public class InstanceService {

	private static Map<String, InstanceStatus> instanceStatusMap = new HashMap<String, InstanceStatus>();

	public synchronized static IStatus updateInstanceStatus(InstanceStatus inst, String remoteAddress) {
		InstanceStatus shis = instanceStatusMap.get(inst.getUuid());
		if (shis != null) {
			inst.setFirstSeen(shis.getFirstSeen());
		} else {
			inst.setFirstSeen(new Date());
		}
		inst.setRemoteAddress(remoteAddress);
		inst.setLastUpdate(new Date());

		instanceStatusMap.put(inst.getUuid(), inst);

		if (inst.getState() == STATE.SHUTTING_DOWN) {
			instanceStatusMap.remove(inst.getUuid());
		}

		return Status.OK_STATUS;
	}

	public static List<InstanceStatus> getInstanceStatus() {
		return new ArrayList<InstanceStatus>(instanceStatusMap.values());
	}

}
