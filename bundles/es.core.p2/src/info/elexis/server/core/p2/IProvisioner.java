package info.elexis.server.core.p2;

import java.net.URI;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.Update;

import info.elexis.server.core.p2.internal.RepoInfo;

public interface IProvisioner {

	public IStatus install(IInstallableUnit unit, IProgressMonitor monitor);

	/**
	 * Install a feature by its id
	 * 
	 * @param feature
	 * @param monitor
	 * @return
	 */
	public IStatus install(String feature, IProgressMonitor monitor);

	/**
	 * Perform updates on a list of installable units generated via
	 * {@link #getAvailableUpdates()}
	 * 
	 * @param updates
	 * @param monitor
	 * @return
	 */
	public IStatus update(Collection<Update> updates, IProgressMonitor monitor);

	public IStatus uninstall(String feature, IProgressMonitor monitor);

	/**
	 * @return all features installed in the current local profile
	 */
	public Collection<IInstallableUnit> getInstalledFeatures();

	/**
	 * @return all features available on the update site
	 * @param 
	 */
	public Collection<IInstallableUnit> getAllAvailableFeatures(IProgressMonitor monitor);

	/**
	 * 
	 * @param
	 * @param id the feature id
	 * @return a single {@link IInstallableUnit} by its id out of the pool of
	 *         {@link #getAllAvailableFeatures()}, <code>null</code> if none found
	 */
	public IInstallableUnit getFeatureInAllAvailableFeatures(IProgressMonitor monitor, String id);

	/**
	 * 
	 * @return the current list of updatable features
	 */
	public Collection<Update> getAvailableUpdates();

	/**
	 * @param iu
	 * @return the locally installed feature for a given {@link IInstallableUnit} or
	 *         <code>null</code> if not available
	 */
	public IInstallableUnit getInstalledFeature(IInstallableUnit iu);

	public void addRepository(URI location, String username, String password);

	/**
	 * 
	 * @param location
	 * @return <code>true</code> if repository was removed or it was already not
	 *         part of the list
	 */
	public boolean removeRepository(URI location);

	/**
	 * 
	 * @return information about the current repository state
	 */
	public RepoInfo getRepositoryInfo();
}
