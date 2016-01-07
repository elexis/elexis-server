package info.elexis.server.core.internal;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;

import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class Configurator {

    private static ConfigurationAdmin configurationAdmin;
    private static Configuration configuration;

    @Activate
    public void start() throws IOException {
        configuration = configurationAdmin.getConfiguration(Activator.PLUGIN_ID, null);
        Dictionary<String, Object> props = configuration.getProperties();
        if (props == null) {
            props = new Hashtable<String, Object>();
            configuration.update(props);
        }
    }

    @Reference
    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
    	Configurator.configurationAdmin = configurationAdmin;
    }

    protected void unsetConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
    	Configurator.configurationAdmin = null;
    }

    /**
     * 
     * @param key
     * @param value if <code>null</code> remove the entry
     */
	public static void setValue(String key, Object value) {
		Dictionary<String, Object> properties = configuration.getProperties();
		if(value==null) {
			properties.remove(key);
		} else {
			properties.put(key, value);
		}
		try {
			configuration.update(properties);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Optional<Object> getValue(String key) {
		Object object = configuration.getProperties().get(key);
		return Optional.ofNullable(object);
	}

}
