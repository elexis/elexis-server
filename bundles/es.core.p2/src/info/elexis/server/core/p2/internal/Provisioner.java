package info.elexis.server.core.p2.internal;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
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
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.p2.Activator;


public class Provisioner {

	public enum UpdateRepository {
		release, beta, snapshot, prerelease
	}

	private Logger log = LoggerFactory.getLogger(Provisioner.class);
	private static UpdateRepository selectedRepository = UpdateRepository.release;

	private IProvisioningAgentProvider agentProvider;
	private IProvisioningAgent agent;
	private IProfileRegistry registry;
	private IArtifactRepositoryManager artifactManager;
	private IMetadataRepositoryManager metadataManager;
	private ProvisioningSession session;

	private List<Update> update = new ArrayList<Update>();
	private UpdateOperation updateOperation = null;

	private LoadRepositoryJob loadingJob = new LoadRepositoryJob();
	private CheckUpdatesJob checkUpdatesJob = new CheckUpdatesJob();

	private final URL p2UpdateSiteUrl;

	public Provisioner() {
		URL temp;
		try {
			temp = getDataLocation().toURL();
		} catch (MalformedURLException e) {
			temp = null;
			// will not happen
		}
		p2UpdateSiteUrl = temp;
	}

	protected void setAgentProvider(IProvisioningAgentProvider value) {
		agentProvider = value;
	}

	/**
	 * 
	 * @return <code>true</code> if the loading was successful
	 */
	private boolean load() {
		if (!updateServiceIsConnectable())
			return false;

		synchronized (loadingJob) {
			if (!loadingJob.isLoaded()) {
				if (loadingJob.isLoading()) {
					try {
						loadingJob.join();
					} catch (InterruptedException e) {
						log.warn("Loading Repository interrupted." + e);
					}
				} else {
					loadingJob.schedule();
					try {
						loadingJob.join();
					} catch (InterruptedException e) {
						log.warn("Loading Repository interrupted." + e);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Check for updates by comparing the locally installed features with the
	 * remotely available features, populating {@link Provisioner#update}
	 */
	private class CheckUpdatesJob extends Job {

		private boolean loaded = false;
		private boolean loading = false;

		public CheckUpdatesJob() {
			super("Check for updates...");
		}

		protected IStatus run(IProgressMonitor monitor) {
			loading = true;

			monitor.beginTask("Check for updates ...", getInstalledFeatures().size());

			BenchmarkTimer bt = new BenchmarkTimer();
			bt.start();

			load();
			update.clear();

			for (IInstallableUnit ii : getInstalledFeatures()) {
				IInstallableUnit replacement = getLatestAvailableFeature(ii);
				if (replacement != null && replacement.compareTo(ii) > 0) {
					update.add(new Update(ii, replacement));
					log.debug("[UPDATE] Found update " + replacement);
				}
				monitor.worked(1);
			}

			bt.end();
			log.debug("Check for updates job took " + bt.geTotalTimeHumanReadableString());

			monitor.done();
			loaded = true;
			loading = false;
			return Status.OK_STATUS;
		}

		public boolean isLoaded() {
			return loaded;
		}

		public boolean isLoading() {
			return loading;
		}

	}

	private class LoadRepositoryJob extends Job {

		private boolean loaded = false;
		private boolean loading = false;

		public LoadRepositoryJob() {
			super("Load update repository...");
		}

		protected IStatus run(IProgressMonitor monitor) {
			loading = true;
			monitor.beginTask("Load update repository...", 2);
			try {

				BenchmarkTimer bt = new BenchmarkTimer();
				bt.start();

				log.info("Loading repository " + p2UpdateSiteUrl);

				metadataManager.loadRepository(getDataLocation(), new SubProgressMonitor(monitor, 1));
				artifactManager.loadRepository(getDataLocation(), new SubProgressMonitor(monitor, 1));
				bt.end();

				log.debug("Load P2 Repository job took " + bt.geTotalTimeHumanReadableString());
			} catch (ProvisionException e) {
				log.error("Unable to load repository " + p2UpdateSiteUrl, e);
				loading = false;
				return Status.CANCEL_STATUS;
			} catch (OperationCanceledException e) {
				log.error("Unable to load repository " + p2UpdateSiteUrl, e);
				loading = false;
				return Status.CANCEL_STATUS;
			}
			loaded = true;
			loading = false;
			monitor.done();
			return Status.OK_STATUS;
		}

		public boolean isLoaded() {
			return loaded;
		}

		public boolean isLoading() {
			return loading;
		}
	}

	/**
	 * Determine the main update site location. This depends on the Medelexis
	 * configuration: snapshot, beta, prerelease or release
	 * 
	 * @return
	 */
	private URI getDataLocation() {
		try {
			String repoName = System.getProperty(Constants.PROVISIONING_UPDATE_REPO);
			if (repoName != null && !repoName.isEmpty()) {
				if (repoName.equalsIgnoreCase(UpdateRepository.snapshot.toString())) {
					selectedRepository = UpdateRepository.snapshot;
				} else if (repoName.equalsIgnoreCase(UpdateRepository.beta.toString())) {
					selectedRepository = UpdateRepository.beta;
				} else if (repoName.equalsIgnoreCase(UpdateRepository.prerelease.toString())) {
					selectedRepository = UpdateRepository.prerelease;
				} else {
					selectedRepository = UpdateRepository.release;
				}
			}

			return new URI(Constants.MAIN_DOWNLOAD_URL + selectedRepository + "/");
		} catch (URISyntaxException e) {
			log.error(
					"Invalid profile location URI - " + Constants.MAIN_DOWNLOAD_URL + selectedRepository + "/");
		}
		return null;
	}

	public Collection<IInstallableUnit> getInstalledFeatures() {
		if (!load())
			return Collections.emptyList();

		IProfile profile = registry.getProfile(IProfileRegistry.SELF);
		if (profile == null) {
			return Collections.emptyList();
		}
		IQueryResult<IInstallableUnit> result = profile.query(QueryUtil.createIUGroupQuery(),
				new NullProgressMonitor());
		// for (IInstallableUnit ii : result.toUnmodifiableSet()) {
		// System.out.println("getInstalled() " + ii.getId() + "_" +
		// ii.getVersion());
		// }
		return result.toUnmodifiableSet();
	}

	private IInstallableUnit findFeature(String feature) {
		List<IInstallableUnit> features = new ArrayList<IInstallableUnit>(getAllAvailableFeatures());
		// sort in order to have the newest feature on top
		Collections.sort(features, Collections.reverseOrder());

		for (Iterator<IInstallableUnit> i = features.iterator(); i.hasNext();) {
			IInstallableUnit unit = (IInstallableUnit) i.next();
			if (unit.getId().equals(feature))
				return unit;
		}
		return null;
	}

	public IStatus install(IInstallableUnit unit, IProgressMonitor monitor) {
		if (!load())
			return Status.OK_STATUS;

		InstallOperation operation = new InstallOperation(session, Arrays.asList(unit));
		IStatus result = operation.resolveModal(monitor);

		handleDebugLogInformation("[INSTALL]", result, unit);

		log.debug("[INSTALL] unit " + unit + " | result " + result.getMessage() + " | severity "
				+ result.getSeverity() + " | code " + result.getCode());

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

	private void handleDebugLogInformation(String header, IStatus result, IInstallableUnit unit) {
		if (result instanceof MultiStatus) {
			MultiStatus ms = (MultiStatus) result;
			IStatus[] children = ms.getChildren();
			for (IStatus iStatus : children) {
				log.debug(header + " MULTISTATUS unit " + unit + " | result " + iStatus.getMessage() + " | severity "
						+ iStatus.getSeverity() + " | code " + iStatus.getCode() + " | plugin " + iStatus.getPlugin());
			}
		} else {
			log.debug(header + " unit " + unit + " | result " + result.getMessage() + " | severity "
					+ result.getSeverity() + " | code " + result.getCode());
		}
	}

	public IStatus install(String feature, IProgressMonitor monitor) {
		IInstallableUnit unit = findFeature(feature);

		if (unit == null) {
			log.error("[INSTALL] Cannot find feature : " + feature);
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Cannot find feature : " + feature);
		}
		return install(unit, monitor);
	}

	private IStatus uninstall(IInstallableUnit unit, IProgressMonitor monitor) {
		if (!load())
			return Status.OK_STATUS;

		UninstallOperation operation = new UninstallOperation(session, Arrays.asList(unit));
		IStatus result = operation.resolveModal(monitor);

		handleDebugLogInformation("[UNINSTALL]", result, unit);

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

	public IStatus uninstall(String feature, IProgressMonitor monitor) {
		if (!load())
			return Status.OK_STATUS;

		IInstallableUnit unit = findFeature(feature);
		if (unit == null) {
			log.error("[UNINSTALL] Cannot find feature : " + feature);
			return new Status(IStatus.ERROR, Activator.BUNDLE_ID, "Cannot find feature : " + feature);
		}

		return uninstall(unit, monitor);
	}

	public Collection<IInstallableUnit> getAllAvailableFeatures() {
		if (!load())
			return Collections.emptyList();

		IQueryResult<IInstallableUnit> result = metadataManager.query(QueryUtil.createIUGroupQuery(),
				new NullProgressMonitor());
		return result.toSet();
	}

	public IInstallableUnit getFeatureInAllAvailableFeatures(String id) {
		Collection<IInstallableUnit> allAvailableFeatures = getAllAvailableFeatures();
		for (IInstallableUnit iu : allAvailableFeatures) {
			if (iu.getId().equalsIgnoreCase(id))
				return iu;
		}
		return null;
	}

	public Collection<IInstallableUnit> getAvailableCategories() {
		if (!load())
			return Collections.emptyList();

		IQueryResult<IInstallableUnit> result = metadataManager.query(QueryUtil.createIUCategoryQuery(),
				new NullProgressMonitor());
		return result.toSet();
	}

	public Collection<IInstallableUnit> getAllFeaturesForCategory(IInstallableUnit category) {
		if (!load())
			return Collections.emptyList();

		IQueryResult<IInstallableUnit> result = metadataManager.query(QueryUtil.createIUCategoryMemberQuery(category),
				new NullProgressMonitor());
		return result.toSet();
	}

	public Collection<IInstallableUnit> getLatestFeaturesForCategory(IInstallableUnit category) {

		IQuery<IInstallableUnit> query = QueryUtil.createIUCategoryMemberQuery(category);
		query = QueryUtil.createLatestQuery(query);

		IQueryResult<IInstallableUnit> result = metadataManager.query(query, new NullProgressMonitor());
		return result.toSet();
	}

	public Collection<IInstallableUnit> getLatestAvailableCategories() {
		if (!load())
			return Collections.emptyList();

		IQuery<IInstallableUnit> query = QueryUtil.createIUCategoryQuery();
		query = QueryUtil.createLatestQuery(query);

		IQueryResult<IInstallableUnit> result = metadataManager.query(query, new NullProgressMonitor());
		return result.toSet();
	}

	public IInstallableUnit getLatestAvailableFeature(IInstallableUnit feature) {
		IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(feature.getId());
		query = QueryUtil.createLatestQuery(query);
		IQueryResult<IInstallableUnit> result = metadataManager.query(query, new NullProgressMonitor());
		Set<IInstallableUnit> resultSet = result.toSet();
		if (resultSet != null && resultSet.size() == 1) {
			return resultSet.iterator().next();
		}
		return null;
	}

	public IInstallableUnit getInstalledFeature(IInstallableUnit iu) {
		IProfile profile = registry.getProfile(IProfileRegistry.SELF);
		if (profile == null) {
			return null;
		}
		IQueryResult<IInstallableUnit> result = profile.query(QueryUtil.createIUQuery(iu.getId()),
				new NullProgressMonitor());

		Set<IInstallableUnit> resultSet = result.toUnmodifiableSet();
		if (resultSet != null && resultSet.size() == 1) {
			return resultSet.iterator().next();
		}
		return null;
	}

	public Collection<Update> getAvailableUpdates() {
		if (!load())
			return Collections.emptyList();

		if (!checkUpdatesJob.isLoaded()) {
			checkUpdatesJob.schedule();
			try {
				checkUpdatesJob.join();
			} catch (InterruptedException e) {
				log.warn("Loading Update Information interrupted." + e);
			}
		}

		return update;
	}

	public IStatus update(Collection<Update> updates, IProgressMonitor monitor) {
		if (!load())
			return Status.OK_STATUS;

		IStatus result = updateOperation.getResolutionResult();

		if (result.isOK()) {
			ProvisioningJob job = updateOperation.getProvisioningJob(monitor);
			job.schedule();
			try {
				job.join();
				result = job.getResult();
				/**
				 * see org.eclipse.equinox.internal.p2.operations.IStatusCodes
				 * for detailed explanation
				 */
				log.debug("[UPDATE] " + result.getMessage() + " | severity " + result.getSeverity() + " | code "
						+ result.getCode());
			} catch (InterruptedException e) {
				// ignore
			}
		}

		return result;
	}

	public IStatus checkForUpdates(Collection<Update> updates, IProgressMonitor monitor) {
		if (!load())
			return Status.OK_STATUS;

		updateOperation = new UpdateOperation(session);
		if (updates != null) {
			updateOperation.setSelectedUpdates(updates.toArray(new Update[0]));
		}
		IStatus result = updateOperation.resolveModal(monitor);
		log.debug("[CHECK_FOR_UPDATE] " + result.getMessage() + " | severity " + result.getSeverity() + " | code "
				+ result.getCode());
		return result;
	}

	public String getUpdateRepository() {
		return selectedRepository.toString();
	}

	public boolean updateServiceIsConnectable() {
		return checkHttpUrlConnectability(p2UpdateSiteUrl);
	}

	private boolean checkHttpUrlConnectability(URL url) {
		if (url == null)
			return false;
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setReadTimeout(1000);
			connection.setConnectTimeout(500);
			connection.setRequestMethod("HEAD");
			return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (IOException e) {
			log.error("Error connecting " + url + ": " + e.getMessage() + " (" + e.getClass() + ")");
			return false;
		}
	}

	public void addAdditionalP2RepositoryLocation(List<String> repositories, IProgressMonitor monitor) {
		load();

		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		URI uri = null;
		try {
			for (String repo : repositories) {
				if (!repo.endsWith("/"))
					repo = repo + "/";
				uri = new URI(repo + selectedRepository + "/");

				if (!checkHttpUrlConnectability(uri.toURL())) {
					log.warn("Cannot connect " + uri.toURL() + " skipping add.");
					continue;
				}

				metadataManager.loadRepository(uri, monitor);
				artifactManager.loadRepository(uri, monitor);

				log.info("Loaded additional P2 repo: " + uri);
			}
		} catch (OperationCanceledException | URISyntaxException | MalformedURLException | ProvisionException e) {
			log.error("Error loading repository ", e);
		}
	}
}
