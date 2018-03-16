package info.elexis.server.core.rest.test.elexisinstances;

import com.eclipsesource.jaxrs.provider.gson.GsonProvider;
import org.glassfish.jersey.client.ClientConfig;

public class ElexisServerClientConfig extends ClientConfig {
	
	public ElexisServerClientConfig(){
		register(GsonProvider.class);
	}
}
