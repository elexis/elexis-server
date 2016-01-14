package info.elexis.server.core.internal;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.connector.elexis.ElexisDBConnection;
import info.elexis.server.core.extension.DBConnection;

@Component(property={"service.pid:String="+Activator.PLUGIN_ID}, immediate=true)
public class ManagedServiceImpl implements ManagedService {

	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		Enumeration<String> keys = (properties!=null) ? properties.keys() : Collections.emptyEnumeration();
		while(keys.hasMoreElements()) {
			String key = keys.nextElement();
			handleConfigUpdateForKey(key, properties.get(key));
		}
	}

	private void handleConfigUpdateForKey(final String key, final Object object) {
		switch (key) {
		case ElexisDBConnection.CONFIG_KEY_CONNECTION:
			DBConnection dbc = DBConnection.unmarshall((String)object);
			ElexisDBConnection.setConnection(dbc);
			break;

		default:
			break;
		}
		
	}

}
