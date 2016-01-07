package info.elexis.server.core.connector.elexis.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.util.DBConnection;
import info.elexis.server.core.connector.elexis.ElexisConnection;

public class ConfigAdminConsumer {
	
	private static Logger log = LoggerFactory.getLogger(ConfigAdminConsumer.class);
	
	private ConfigurationAdmin configAdmin;

	public synchronized void bind(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;

		performJPAIncrementalUpdate();
	}

	public synchronized void unbind(ConfigurationAdmin configAdmin) {
		configAdmin = null;
	}

	/**
	 * perform the incremental update of the elexis persistence unit defined in
	 * bundle info.elexis.server.core.connector.elexis.jpa
	 * 
	 * @see https://wiki.eclipse.org/Gemini/JPA/Documentation/OtherTopics#
	 *      Incremental_Configuration
	 */
	private void performJPAIncrementalUpdate() {
		try {
			// TODO perform this on new set of connection

			Configuration config = configAdmin.createFactoryConfiguration("gemini.jpa.punit", null);
			Dictionary<String, String> props = new Hashtable<String, String>();

			props.put("gemini.jpa.punit.name", "elexis");

			DBConnection connection = ElexisConnection.getConnection();

			props.put("javax.persistence.jdbc.driver", connection.rdbmsType.driverName);
			props.put("javax.persistence.jdbc.url", connection.connectionString);
			props.put("javax.persistence.jdbc.user", connection.username);
			props.put("javax.persistence.jdbc.password", connection.password);

			config.update(props);
		} catch (IOException e) {
			log.error("Error configuration JPA connection", e);
		}
	}
}
