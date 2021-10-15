package info.elexis.server.core.connector.elexis.internal.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.common.InstanceStatus.STATE;

public class InstanceService {

	private static Map<String, InstanceStatus> instanceStatusMap = Collections
			.synchronizedMap(new HashMap<String, InstanceStatus>());

	public static IStatus updateInstanceStatus(InstanceStatus inst, String remoteAddress) {
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
		ArrayList<InstanceStatus> arrayList = new ArrayList<InstanceStatus>(instanceStatusMap.values());
		Collections.sort(arrayList, new Comparator<InstanceStatus>() {
			@Override
			public int compare(InstanceStatus o1, InstanceStatus o2) {
				return o1.getLastUpdate().compareTo(o2.getLastUpdate());
			}
		});
		return arrayList;
	}

	public static void clearInstanceStatus() {
		instanceStatusMap.clear();
	}

}
