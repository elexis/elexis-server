package info.elexis.server.core.connector.elexis.jpa;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.connector.elexis.jpa.internal.Activator;
import info.elexis.server.core.connector.elexis.jpa.manager.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Config;
import info.elexis.server.core.extension.IElexisConnector;

@Component(immediate = true)
public class JpaElexisConnector implements IElexisConnector {
	
	@Override
	public IStatus getElexisDBStatusInformation() {

		Config cDBV = ElexisEntityManager.getEntityManager().find(Config.class, "dbversion");
		if (cDBV == null) {
			return new Status(Status.ERROR, Activator.BUNDLE_ID, "Error loading config");
		}

		String dbv = cDBV.getWert();
		String elVersion = ElexisEntityManager.getEntityManager().find(Config.class, "ElexisVersion").getWert();
		String created = ElexisEntityManager.getEntityManager().find(Config.class, "created").getWert();
		String statusInfo = "Elexis " + elVersion + ", DB " + dbv + " (" + created + ")";

		return new Status(Status.OK, Activator.BUNDLE_ID, statusInfo);
	}

}
