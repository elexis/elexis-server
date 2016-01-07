package info.elexis.server.core.extension;

import org.eclipse.core.runtime.IStatus;

public interface IElexisConnector {

	public abstract IStatus getElexisDBStatusInformation();

}
