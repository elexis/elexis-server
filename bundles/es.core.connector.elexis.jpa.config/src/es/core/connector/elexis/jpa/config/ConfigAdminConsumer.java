package es.core.connector.elexis.jpa.config;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.ElexisDBConnection;
import info.elexis.server.core.constants.ESEventConstants;
import info.elexis.server.core.extension.DBConnection;

@Component(property={"event.topics:String="+ESEventConstants.UPDATE_DB_CONNECTION}, immediate=true)
public class ConfigAdminConsumer implements EventHandler {
	
	private static Logger log = LoggerFactory.getLogger(ConfigAdminConsumer.class);
	
	private ConfigurationAdmin configAdmin;

	@Reference(name="config.admin")
	protected synchronized void bind(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;
	}
	
	protected synchronized void unbind(ConfigurationAdmin configAdmin) {
		this.configAdmin = null;
	}
	
	@Override
	public void handleEvent(Event event) {
		performJPAIncrementalUpdate();
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
			Configuration config = configAdmin.createFactoryConfiguration("gemini.jpa.punit", null);
			Dictionary<String, String> props = new Hashtable<String, String>();

			props.put("gemini.jpa.punit.name", "elexis");

			Optional<DBConnection> connection = ElexisDBConnection.getConnection();
			
			if(!connection.isPresent()) {
				log.warn("ElexisDBConnection was null, did not update JPA configuration");
				return;
			}

			props.put("javax.persistence.jdbc.driver", connection.get().rdbmsType.driverName);
			props.put("javax.persistence.jdbc.url", connection.get().connectionString);
			props.put("javax.persistence.jdbc.user", connection.get().username);
			props.put("javax.persistence.jdbc.password", connection.get().password);

			config.update(props);
		} catch (IOException e) {
			log.error("Error configuration JPA connection", e);
		}
	}

}
