package info.elexis.server.core.connector.elexis.common;

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.core.connector.elexis.internal.BundleConstants;

public class ElexisDBConnection {
	
	private static IModelService modelService =
		OsgiServiceUtil.getService(IModelService.class).get();
	
	public static Optional<DBConnection> getConnection(){
		return ElexisDBConnectionUtil.getConnection();
	}
	
	public static IStatus getDatabaseInformation(){
		String statusInfo = getDatabaseInformationString();
		return new Status(Status.OK, BundleConstants.BUNDLE_ID, statusInfo);
	}
	
	public static String getDatabaseInformationString(){
		Optional<IConfig> config = modelService.load("dbversion", IConfig.class);
		if (!config.isPresent()) {
			return "Could not find dbversion entry in config table.";
		}
		
		String connectionString = "null";
		if (ElexisDBConnectionUtil.getConnection().isPresent()) {
			connectionString = ElexisDBConnectionUtil.getConnection().get().connectionString;
		}
		String dbv = config.get().getValue();
		String elVersion = modelService.load("ElexisVersion", IConfig.class).get().getValue();
		Optional<IConfig> createdConfig = modelService.load("created", IConfig.class);
		String created = (createdConfig != null) ? createdConfig.get().getValue() : "";
		String statusInfo = "Elexis " + elVersion + " DBv " + dbv + ", created " + created + " ["
			+ connectionString + "]";
		return statusInfo;
		
	}
}
