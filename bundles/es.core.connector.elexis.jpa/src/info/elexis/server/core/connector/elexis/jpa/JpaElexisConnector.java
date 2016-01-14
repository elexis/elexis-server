package info.elexis.server.core.connector.elexis.jpa;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import info.elexis.server.core.connector.elexis.jpa.internal.Activator;
import info.elexis.server.core.connector.elexis.jpa.manager.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;

@Deprecated 
public class JpaElexisConnector  {
	
	public IStatus getElexisDBStatusInformation() {

		Config cDBV = ElexisEntityManager.em().find(Config.class, "dbversion");
		if (cDBV == null) {
			return new Status(Status.ERROR, Activator.BUNDLE_ID, "Error loading config");
		}

		String dbv = cDBV.getWert();
		String elVersion = ElexisEntityManager.em().find(Config.class, "ElexisVersion").getWert();
		String created = ElexisEntityManager.em().find(Config.class, "created").getWert();
		String statusInfo = "Elexis " + elVersion + ", DB " + dbv + " (" + created + ")";

		return new Status(Status.OK, Activator.BUNDLE_ID, statusInfo);
	}

}
