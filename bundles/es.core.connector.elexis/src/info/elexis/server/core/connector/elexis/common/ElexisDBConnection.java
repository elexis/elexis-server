package info.elexis.server.core.connector.elexis.common;

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.services.IModelService;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;
import info.elexis.server.core.connector.elexis.internal.services.CoreModelServiceHolder;

public class ElexisDBConnection {
	
	public static Optional<DBConnection> getConnection(){
		return ElexisDBConnectionUtil.getConnection();
	}
	
	public static IStatus getDatabaseInformation(){
		String statusInfo = getDatabaseInformationString();
		return new Status(Status.OK, BundleConstants.BUNDLE_ID, statusInfo);
	}
	
	public static String getDatabaseInformationString(){
		IModelService modelService = CoreModelServiceHolder.get();
		if (modelService == null) {
			return "No database connection set.";
		}
		
		Optional<IConfig> dbVersion = modelService.load("dbversion", IConfig.class);
		if (!dbVersion.isPresent()) {
			return "Could not find dbversion entry in config table.";
		}
		
		String connectionString = "null";
		if (ElexisDBConnectionUtil.getConnection().isPresent()) {
			connectionString = ElexisDBConnectionUtil.getConnection().get().connectionString;
		}
		
		Optional<IConfig> _elVersion = modelService.load("ElexisVersion", IConfig.class);
		String elVersion = (_elVersion.isPresent()) ? _elVersion.get().getValue() : "unknown";
		Optional<IConfig> _created = modelService.load("created", IConfig.class);
		String created = (_created.isPresent()) ? _created.get().getValue() : "unknown";
		String statusInfo = "Elexis " + elVersion + " DBv " + dbVersion.get().getValue()
			+ ", created " + created + " [" + connectionString + "]";
		return statusInfo;
		
	}
}
