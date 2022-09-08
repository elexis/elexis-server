package info.elexis.server.core.connector.elexis.common;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import ch.elexis.core.model.IConfig;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ElexisDBConnection {

	public static String getDatabaseInformationString() {
		IModelService modelService = CoreModelServiceHolder.get();
		if (modelService == null) {
			return "No database connection set.";
		}

		Optional<IConfig> dbVersion = modelService.load("dbversion", IConfig.class);
		if (!dbVersion.isPresent()) {
			return "Could not find dbversion entry in config table.";
		}

		Optional<IConfig> _elVersion = modelService.load("ElexisVersion", IConfig.class);
		String elVersion = (_elVersion.isPresent()) ? _elVersion.get().getValue() : "unknown";
		Optional<IConfig> _created = modelService.load("created", IConfig.class);
		String created = (_created.isPresent()) ? _created.get().getValue() : "unknown";

		String dbMetaData = "";
		Optional<DataSource> dataSource = OsgiServiceUtil.getService(DataSource.class, "(id=default)");
		if (dataSource.isPresent()) {
			try {
				DatabaseMetaData dbmd = dataSource.get().getConnection().getMetaData();
				dbMetaData += dbmd.getDatabaseProductName();
				dbMetaData += " " + dbmd.getDatabaseProductVersion();
				dbMetaData += " @ " + dbmd.getURL();
			} catch (SQLException e) {
			}
		}

		String statusInfo = "Elexis " + elVersion + " DBv " + dbVersion.get().getValue() + ", created " + created + " ["
				+ dbMetaData + "] ";
		return statusInfo;

	}
}
