package info.elexis.server.core.connector.elexis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;

import info.elexis.server.core.constants.ESEventConstants;
import info.elexis.server.core.extension.IElexisConnector;
import info.elexis.server.core.internal.Activator;
import info.elexis.server.core.internal.EventAdminConsumer;

@Component
public class ElexisConnectorManager {

	private static IElexisConnector systemConnector;

	private static List<IElexisConnector> elexisConnectors = new ArrayList<IElexisConnector>();

	@Reference(
			name = "ElexisConnector",
            service = IElexisConnector.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.STATIC,
            unbind = "unbind"
    )
	protected synchronized void bind(IElexisConnector aec) {
		elexisConnectors.add(aec);
	}

	protected synchronized void unbind(IElexisConnector aec) {
		elexisConnectors.remove(aec);
	}

	public static List<IElexisConnector> getElexisConnectors() {
		return elexisConnectors;
	}

	public static void setSystemConnector(IElexisConnector systemConnector) {
		ElexisConnectorManager.systemConnector = systemConnector;
		Event updateConfig = new Event(ESEventConstants.UPDATE_DB_CONNECTION, Collections.emptyMap());
		EventAdminConsumer.getEventAdmin().sendEvent(updateConfig);
	}

	public static Optional<IElexisConnector> getElexisConnectorByClassName(String className) {
		return ElexisConnectorManager.getElexisConnectors().stream()
				.filter(p -> p.getClass().getName().equalsIgnoreCase(className)).findFirst();
	}

	public static IElexisConnector getSystemConnector() {
		return systemConnector;
	}

	public static IStatus getConnectionStatusInformation() {
		if (elexisConnectors.size() > 0) {
			IElexisConnector ec = elexisConnectors.get(0);
			return ec.getElexisDBStatusInformation();
		}
		return new Status(Status.ERROR, Activator.PLUGIN_ID, "No connector configured.");
	}
}
