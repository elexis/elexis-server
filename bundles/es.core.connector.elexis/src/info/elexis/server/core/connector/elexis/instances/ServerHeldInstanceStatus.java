package info.elexis.server.core.connector.elexis.instances;

import java.time.LocalDateTime;

import ch.elexis.core.common.InstanceStatus;

public class ServerHeldInstanceStatus {

	private final LocalDateTime firstSeen;
	private LocalDateTime lastUpdate;
	private String remoteAddress;

	private InstanceStatus instanceStatus;

	public ServerHeldInstanceStatus() {
		firstSeen = LocalDateTime.now();
		lastUpdate = LocalDateTime.now();
	}

	public InstanceStatus getInstanceStatus() {
		return instanceStatus;
	}

	public void setInstanceStatus(InstanceStatus instanceStatus) {
		this.instanceStatus = instanceStatus;
	}

	public LocalDateTime getFirstSeen() {
		return firstSeen;
	}

	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(LocalDateTime lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	public String getRemoteAddress() {
		return remoteAddress;
	}
	
	public void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}
}
