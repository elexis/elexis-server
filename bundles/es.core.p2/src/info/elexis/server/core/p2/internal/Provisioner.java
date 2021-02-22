package info.elexis.server.core.p2.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.core.spi.IAgentServiceFactory;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.InstallOperation;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UninstallOperation;
import org.eclipse.equinox.p2.operations.Update;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.query.IQuery;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.IRepository;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.status.StatusUtil;
import info.elexis.server.core.p2.Constants;
import info.elexis.server.core.p2.IProvisioner;

@Component(immediate = true)
public class Provisioner implements IProvisioner {

	private Logger log;

	private IProvisioningAgentProvider agentProvider;
	private IProvisioningAgent agent;

	private IProfileRegistry registry;
	private IMetadataRepositoryManager metadataRepositoryManager;
	private IArtifactRepositoryManager artifactRepositoryManager;

	@Reference(service = IProvisioningAgentProvider.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "unsetAgentProvider")
	protected void setAgentProvider(IProvisioningAgentProvider agentProvider) {
		this.agentProvider = agentProvider;
	}
	
	@Reference(target = "(p2.agent.servicename=org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager)")
	protected void setMetadataRepositoryManager(IAgentServiceFactory serviceFactory){
		// is necessary to have the service loaded on startup
	}

	protected void unsetAgentProvider(IProvisioningAgentProvider agentProvider) {
		this.agentProvider = null;
	}

	@Activate
	public void activate(ComponentContext context) throws Exception {

		log = LoggerFactory.getLogger(getClass());

		agent = agentProvider.createAgent(null);

		registry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
		metadataRepositoryManager = (IMetadataRepositoryManager) agent
				.getService(IMetadataRepositoryManager.SERVICE_NAME);
		artifactRepositoryManager = (IArtifactRepositoryManager) agent
				.getService(IArtifactRepositoryManager.SERVICE_NAME);
	}

	@Deactivate
	public void deactivate(ComponentContext context) throws Exception {
		registry = null;
	}

	private void refreshRepositories() {
		for (URI repo : metadataRepositoryManager.getKnownRepositories(0)) {
			try {
				metadataRepositoryManager.refreshRepository(repo, new NullProgressMonitor());
			} catch (ProvisionException | OperationCanceledException e) {
				log.warn("Exception refreshing repo " + repo, e);
			}
		}

		for (URI repo : artifactRepositoryManager.getKnownRepositories(0))
			try {
				artifactRepositoryManager.refreshRepository(repo, new NullProgressMonitor());
			} catch (ProvisionException | OperationCanceledException e) {
				log.warn("Exception refreshing repo " + repo, e);
			}
	}

	public IProvisioningAgent getProvisioningAgent() {
		return this.agent;
	}

	@Override
	public IStatus install(IInstallableUnit unit, IProgressMonitor monitor) {
		ProvisioningSession session = new ProvisioningSession(agent);
		InstallOperation operation = new InstallOperation(session, Arrays.asList(unit));
		IStatus result = operation.resolveModal(monitor);
		StatusUtil.logStatus("installResolveResult", log, result);

		log.debug("[INSTALL] unit {} " + unit + " | result " + result.getMessage() + " | severity "
				+ result.getSeverity() + " | code " + result.getCode());

		if (result.isOK()) {
			ProvisioningJob job = operation.getProvisioningJob(monitor);
			job.schedule();
			try {
				job.join();
				result = job.getResult();
				StatusUtil.logStatus("installJobResult", log, result);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		return result;
	}

	@Override
	public IStatus install(String feature, IProgressMonitor monitor) {
		IInstallableUnit unit = findFeature(feature);

		if (unit == null) {
			log.error("[INSTALL] Cannot find feature : {}", feature);
			return new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Cannot find feature : " + feature);
		}
		return install(unit, monitor);
	}

	private IInstallableUnit findFeature(String feature) {
		List<IInstallableUnit> features = new ArrayList<IInstallableUnit>(getAllAvailableFeatures(null));
		// sort in order to have the newest feature on top
		Collections.sort(features, Collections.reverseOrder());

		for (Iterator<IInstallableUnit> i = features.iterator(); i.hasNext();) {
			IInstallableUnit unit = i.next();
			if (unit.getId().equals(feature))
				return unit;
		}
		return null;
	}

	@Override
	public IStatus update(Collection<Update> updates, IProgressMonitor monitor) {
		ProvisioningSession session = new ProvisioningSession(agent);
		List<IInstallableUnit> effectiveUpdates = updates.stream().map(ii -> ii.toUpdate).collect(Collectors.toList());
		UpdateOperation updateOperation = new UpdateOperation(session, effectiveUpdates);
		IStatus result = updateOperation.resolveModal(monitor);
		StatusUtil.logStatus("updateResolutionResult", log, result);
		if (result.isOK()) {
			ProvisioningJob job = updateOperation.getProvisioningJob(monitor);
			job.schedule();
			try {
				job.join();
				result = job.getResult();
				StatusUtil.logStatus("updateJobResult", log, result);
			} catch (InterruptedException e) {
				// ignore
			}
		}
		return result;
	}

	@Override
	public IStatus uninstall(String feature, IProgressMonitor monitor) {
		IInstallableUnit unit = findFeature(feature);

		if (unit == null) {
			log.error("[INSTALL] Cannot find feature : {}", feature);
			return new Status(IStatus.ERROR, Constants.PLUGIN_ID, "Cannot find feature : " + feature);
		}
		return uninstall(unit, monitor);
	}

	private IStatus uninstall(IInstallableUnit unit, IProgressMonitor monitor) {
		ProvisioningSession session = new ProvisioningSession(agent);
		UninstallOperation operation = new UninstallOperation(session, Arrays.asList(unit));
		IStatus result = operation.resolveModal(monitor);
		StatusUtil.logStatus("uninstallResolveResult", log, result);

		if (result.isOK()) {
			ProvisioningJob job = operation.getProvisioningJob(monitor);
			job.schedule();
			try {
				job.join();
				result = job.getResult();
			} catch (InterruptedException e) {
				// ignore
			}
		}

		return result;
	}

	@Override
	public Collection<IInstallableUnit> getInstalledFeatures() {
		IProfile profile = registry.getProfile(IProfileRegistry.SELF);
		if (profile == null) {
			return Collections.emptyList();
		}
		IQueryResult<IInstallableUnit> result = profile.query(QueryUtil.createIUGroupQuery(),
				new NullProgressMonitor());
		return result.toUnmodifiableSet();
	}

	@Override
	public Collection<IInstallableUnit> getAllAvailableFeatures(IProgressMonitor monitor) {
		if(monitor == null) {
			monitor = new NullProgressMonitor();
		}
		IQueryResult<IInstallableUnit> result =
			metadataRepositoryManager.query(QueryUtil.createIUGroupQuery(), monitor);
		return result.toSet();
	}

	@Override
	public IInstallableUnit getFeatureInAllAvailableFeatures(IProgressMonitor monitor, String id){
		Collection<IInstallableUnit> allAvailableFeatures = getAllAvailableFeatures(monitor);
		for (IInstallableUnit iu : allAvailableFeatures) {
			if (iu.getId().equalsIgnoreCase(id))
				return iu;
		}
		return null;
	}

	@Override
	public Collection<Update> getAvailableUpdates() {
		refreshRepositories();
		List<Update> update = new ArrayList<>();
		for (IInstallableUnit ii : getInstalledFeatures()) {
			IInstallableUnit replacement = getLatestAvailableFeature(ii);
			if (replacement != null && replacement.compareTo(ii) > 0) {
				update.add(new Update(ii, replacement));
			}
		}
		return update;
	}

	private IInstallableUnit getLatestAvailableFeature(IInstallableUnit feature) {
		IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(feature.getId());
		query = QueryUtil.createLatestQuery(query);
		IQueryResult<IInstallableUnit> result = metadataRepositoryManager.query(query, new NullProgressMonitor());
		Set<IInstallableUnit> resultSet = result.toSet();
		if (resultSet != null && resultSet.size() == 1) {
			return resultSet.iterator().next();
		}
		return null;
	}

	@Override
	public IInstallableUnit getInstalledFeature(IInstallableUnit iu) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addRepository(URI location, String username, String password) {
		if (!artifactRepositoryManager.contains(location)) {
			artifactRepositoryManager.addRepository(location);
		}

		if (!metadataRepositoryManager.contains(location)) {
			metadataRepositoryManager.addRepository(location);
			log.debug("Added artifact repository {}", location);
		}

		registerHttpAuthentication(location, username, password);
	}
	
	@Override
	public IStatus loadRepository(IProgressMonitor monitor, URI location){
		try {
			metadataRepositoryManager.loadRepository(location, monitor);
			return Status.OK_STATUS;
		} catch (ProvisionException | OperationCanceledException e) {
			return new Status(Status.ERROR, "info.elexis.server.core.p2", e.getMessage(), e);
		}
	}

	private void registerHttpAuthentication(URI location, String username, String password) {
		if (username == null || password == null) {
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

	@Override
	public boolean removeRepository(URI location) {
		boolean result = true;

		if (artifactRepositoryManager.contains(location)) {
			result &= artifactRepositoryManager.removeRepository(location);
			log.debug("Removed artifact repository {}", location);
		}

		if (metadataRepositoryManager.contains(location)) {
			result &= metadataRepositoryManager.removeRepository(location);
			log.debug("Removed metadata repository {}", location);
		}

		return result;
	}

	@Override
	public RepoInfo getRepositoryInfo() {
		int filter = IRepositoryManager.REPOSITORIES_ALL;

		RepoInfo info = new RepoInfo();
		IMetadataRepository repo;
		for (URI repoLoc : metadataRepositoryManager.getKnownRepositories(filter)) {
			String repoName;
			try {
				repo = metadataRepositoryManager.loadRepository(repoLoc, new TimeoutProgressMonitor(2000));
				repoName = repo.getName();
			} catch (OperationCanceledException localOperationCanceledException) {
				log.warn("timeout", localOperationCanceledException);
				repoName = "timeout";
			} catch (ProvisionException localProvisionException) {
				log.warn("FAIL", localProvisionException);
				repoName = "FAILED: " + localProvisionException.getLocalizedMessage();
			}
			info.addMetadataRepoElement(repoName, repoLoc);
		}

		IArtifactRepository artRepo;
		for (URI repoLoc : artifactRepositoryManager.getKnownRepositories(filter)) {
			String repoName;
			try {
				artRepo = artifactRepositoryManager.loadRepository(repoLoc, new TimeoutProgressMonitor(2000));
				repoName = artRepo.getName();
			} catch (OperationCanceledException localOperationCanceledException) {
				log.warn("timeout", localOperationCanceledException);
				repoName = "timeout";
			} catch (ProvisionException localProvisionException) {
				log.warn("FAIL", localProvisionException);
				repoName = "FAILED: " + localProvisionException.getLocalizedMessage();
			}
			info.addArtifactRepoElement(repoName, repoLoc);
		}
		return info;
	}
}
