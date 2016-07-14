
package info.elexis.server.core.contrib;

public interface IApplicationShutdownListener {

	/**
	 * Notifies that the workbench is about to shut down.
	 * 
	 * @param forced
	 *            <code>true</code> if the application is being forced to
	 *            shutdown, <code>false</code> for a regular close
	 * @return <code>null</code> to allow the application to proceed with
	 *         shutdown, a reason string to veto a non-forced shutdown
	 */
	public String performShutdown(boolean forced);

}
