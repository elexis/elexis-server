package info.elexis.server.core.p2;

import java.net.URI;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.Update;

import info.elexis.server.core.p2.internal.RepoInfo;

public interface IProvisioner {

	IStatus install(IInstallableUnit unit, IProgressMonitor monitor);

	/**
	 * Install a feature by its id
	 * 
	 * @param feature
	 * @param monitor
	 * @return
	 */
	IStatus install(String feature, IProgressMonitor monitor);

	/**
	 * Perform updates on a list of installable units generated via
	 * {@link #getAvailableUpdates()}
	 * 
	 * @param updates
	 * @param monitor
	 * @return
	 */
	IStatus update(Collection<Update> updates, IProgressMonitor monitor);

	IStatus uninstall(String feature, IProgressMonitor monitor);

	/**
	 * @return all features installed in the current local profile
	 */
	Collection<IInstallableUnit> getInstalledFeatures();

	/**
	 * @return all features available on the update site
	 * @param
	 */
	Collection<IInstallableUnit> getAllAvailableFeatures(IProgressMonitor monitor);

	/**
	 * 
	 * @param
	 * @param id the feature id
	 * @return a single {@link IInstallableUnit} by its id out of the pool of
	 *         {@link #getAllAvailableFeatures()}, <code>null</code> if none found
	 */
	IInstallableUnit getFeatureInAllAvailableFeatures(IProgressMonitor monitor, String id);

	/**
	 * 
	 * @return the current list of updatable features
	 */
	Collection<Update> getAvailableUpdates();

	/**
	 * @param iu
	 * @return the locally installed feature for a given {@link IInstallableUnit} or
	 *         <code>null</code> if not available
	 */
	IInstallableUnit getInstalledFeature(IInstallableUnit iu);

	/**
	 * Add a repository (does not load it, use
	 * {@link #loadRepository(IProgressMonitor, URI)} to check)
	 * 
	 * @param location
	 * @param username
	 * @param password
	 */
	void addRepository(URI location, String username, String password);

	/**
	 * Try to load a repository
	 * 
	 * @param monitor
	 * @param location
	 * @return
	 */
	IStatus loadRepository(IProgressMonitor monitor, URI location);

	/**
	 * 
	 * @param location
	 * @return <code>true</code> if repository was removed or it was already not
	 *         part of the list
	 */
	boolean removeRepository(URI location);

	/**
	 * 
	 * @return information about the current repository state
	 */
	RepoInfo getRepositoryInfo();
}
