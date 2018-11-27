package info.elexis.server.webapp;

import org.osgi.service.component.annotations.Component;

@Component(service = ConnectorDatabaseConfigurationWebappStatic.class, property = {
		"osgi.http.whiteboard.resource.pattern=/web/elexis-connector/connection/static/*",
		"osgi.http.whiteboard.resource.prefix=/web/elexis-connector/connection/static" })
public class ConnectorDatabaseConfigurationWebappStatic {
}
