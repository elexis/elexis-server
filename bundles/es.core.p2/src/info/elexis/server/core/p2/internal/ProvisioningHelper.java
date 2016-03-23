package info.elexis.server.core.p2.internal;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.ProfileChangeOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.common.status.StatusUtil;

public class ProvisioningHelper {

	private static Logger log = LoggerFactory.getLogger(ProvisioningHelper.class);

	static void refreshRepositories() {
		IMetadataRepositoryManager metadataRepoMgr = Provisioner.getInstance().getMetadataRepositoryManager();
		for (URI repo : metadataRepoMgr.getKnownRepositories(0)) {
			try {
				metadataRepoMgr.refreshRepository(repo, new NullProgressMonitor());
			} catch (ProvisionException | OperationCanceledException e) {
				log.error("Exception refreshing repo " + repo, e);
			}
		}

		IArtifactRepositoryManager artifactRepoMgr = Provisioner.getInstance().getArtifactRepositoryManager();
		for (URI repo : artifactRepoMgr.getKnownRepositories(0))
			try {
				artifactRepoMgr.refreshRepository(repo, new NullProgressMonitor());
			} catch (ProvisionException | OperationCanceledException e) {
				log.error("Exception refreshing repo " + repo, e);
			}
	}

	static IStatus performOperation(ProfileChangeOperation op) {
		ProvisioningJob job = op.getProvisioningJob(new NullProgressMonitor());
		return job.runModal(new NullProgressMonitor());
	}

	/**
	 * 
	 * @param location
	 * @return <code>true</code> if repository was removed or it was already not
	 *         part of the list
	 */
	public static boolean removeRepository(URI location) {
		boolean result = true;

		IArtifactRepositoryManager artifactRepoMgr = Provisioner.getInstance().getArtifactRepositoryManager();
		if (artifactRepoMgr.contains(location)) {
			result &= artifactRepoMgr.removeRepository(location);
			log.debug("Removed artifact repository " + location);
		}

		IMetadataRepositoryManager metadataRepoMgr = Provisioner.getInstance().getMetadataRepositoryManager();
		if (metadataRepoMgr.contains(location)) {
			result &= metadataRepoMgr.removeRepository(location);
			log.debug("Removed metadata repository " + location);
		}

		return result;
	}

	public static void addRepository(URI location, String username, String password) {
		IArtifactRepositoryManager artifactRepoMgr = Provisioner.getInstance().getArtifactRepositoryManager();
		if (!artifactRepoMgr.contains(location)) {
			artifactRepoMgr.addRepository(location);
		}

		IMetadataRepositoryManager metadataRepoMgr = Provisioner.getInstance().getMetadataRepositoryManager();
		if (!metadataRepoMgr.contains(location)) {
			metadataRepoMgr.addRepository(location);
			log.debug("Added artifact repository " + location);
		}
		
		registerHttpAuthentication(location, username, password);
	}

	public static IStatus updateAllFeatures() {
		IProvisioningAgent agent = Provisioner.getInstance().getProvisioningAgent();
		ProvisioningHelper.refreshRepositories();

		IProfileRegistry registry = Provisioner.getInstance().getProfileRegistry();
		IProfile profile = registry.getProfile(IProfileRegistry.SELF);
		Assert.isNotNull(profile);
		ProvisioningSession session = new ProvisioningSession(agent);

		IQuery<IInstallableUnit> query = QueryUtil.createIUAnyQuery();
		IQueryResult<IInstallableUnit> units = profile.query(query, new NullProgressMonitor());

		UpdateOperation operation = new UpdateOperation(session, units.toUnmodifiableSet());
		IStatus status = operation.resolveModal(new TimeoutProgressMonitor(15000));
		log.debug("CHECK_FOR_UPDATE {} | severity {} | code {}", status.getMessage(), status.getSeverity(),
				status.getCode());
		if ((!status.isOK() && status.getCode() == 10000 && status.getSeverity() == 1)) {
			// no updates available
			return status;
		}

		if (status.getSeverity() != IStatus.ERROR) {
			IStatus stat = ProvisioningHelper.performOperation(operation);
			log.info("UPDATED {} / {}", stat.getCode(), stat.getMessage());
			// TODO Show single updates
			// TODO perform restart
			if (stat.isMultiStatus()) {
				StatusUtil.printStatus(log, stat);
			}
		} else {
			log.warn("UPDATE FAILED {} / {}", status.getCode(), status.getMessage());
			if (status.isMultiStatus()) {
				StatusUtil.printStatus(log, status);
			}
		}

		return status;
	}

	public static Collection<IInstallableUnit> getAllInstalledFeatures() {
		IProfileRegistry registry = Provisioner.getInstance().getProfileRegistry();
		IProfile profile = registry.getProfile(IProfileRegistry.SELF);
		if (profile == null) {
			return Collections.emptyList();
		}
		IQueryResult<IInstallableUnit> result = profile.query(QueryUtil.createIUGroupQuery(),
				new NullProgressMonitor());
		return result.toUnmodifiableSet();

	}

	/**
	 * Register the HTTP authentication against a given location
	 * 
	 * @param password
	 * @param username
	 * @param location
	 */
	private static void registerHttpAuthentication(URI location, String username, String password) {
		if(username==null || password == null) {
			return;
		}
		try {
			ISecurePreferences secPref = SecurePreferencesFactory.getDefault()
					.node("org.eclipse.equinox.p2.repository/" + location.getHost());
			secPref.put(IRepository.PROP_USERNAME, username, false);
			secPref.put(IRepository.PROP_PASSWORD, password, true);
		} catch (StorageException e) {
			log.error("Error initializing secure preferences", e);
		}
	}
}
