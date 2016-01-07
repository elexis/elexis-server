package info.elexis.server.core.connector.elexis;

import java.util.Collections;
import java.util.Optional;

import org.osgi.service.event.Event;

import info.elexis.server.core.constants.ESEventConstants;
import info.elexis.server.core.extension.DBConnection;
import info.elexis.server.core.internal.Configurator;
import info.elexis.server.core.internal.EventAdminConsumer;

public class ElexisDBConnection {

	public static final String CONFIG_KEY_CONNECTION = "elexis.connection";

	private static DBConnection connection;

	public static Optional<DBConnection> getConnection() {
		return Optional.ofNullable(connection);
	}

	public static void setConnection(DBConnection connection) {
		ElexisDBConnection.connection = connection;
		
		Event updateConfig = new Event(ESEventConstants.UPDATE_DB_CONNECTION, Collections.emptyMap());
		EventAdminConsumer.getEventAdmin().sendEvent(updateConfig);
	}

	public static void updateConnection(DBConnection connect) {
		// will call ElexisDBConnection.setConnection
		Configurator.setValue(CONFIG_KEY_CONNECTION, connect.marshallIntoString());
	}

}
