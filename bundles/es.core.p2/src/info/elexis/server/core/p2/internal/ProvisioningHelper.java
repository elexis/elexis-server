//package info.elexis.server.core.p2.internal;
//
//import java.net.URI;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Set;
//
//import org.eclipse.core.runtime.Assert;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.NullProgressMonitor;
//import org.eclipse.core.runtime.OperationCanceledException;
//import org.eclipse.core.runtime.Status;
//import org.eclipse.equinox.p2.core.IProvisioningAgent;
//import org.eclipse.equinox.p2.core.ProvisionException;
//import org.eclipse.equinox.p2.engine.IProfile;
//import org.eclipse.equinox.p2.engine.IProfileRegistry;
//import org.eclipse.equinox.p2.metadata.IInstallableUnit;
//import org.eclipse.equinox.p2.operations.InstallOperation;
//import org.eclipse.equinox.p2.operations.ProfileChangeOperation;
//import org.eclipse.equinox.p2.operations.ProvisioningJob;
//import org.eclipse.equinox.p2.operations.ProvisioningSession;
//import org.eclipse.equinox.p2.operations.UninstallOperation;
//import org.eclipse.equinox.p2.operations.Update;
//import org.eclipse.equinox.p2.operations.UpdateOperation;
//import org.eclipse.equinox.p2.query.IQuery;
//import org.eclipse.equinox.p2.query.IQueryResult;
//import org.eclipse.equinox.p2.query.QueryUtil;
//import org.eclipse.equinox.p2.repository.IRepository;
//import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
//import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
//import org.eclipse.equinox.security.storage.ISecurePreferences;
//import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
//import org.eclipse.equinox.security.storage.StorageException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import ch.elexis.core.status.StatusUtil;
//
//public class ProvisioningHelper {
//
//	private static Logger log = LoggerFactory.getLogger(ProvisioningHelper.class);
//
//	
//
//	static IStatus performOperation(ProfileChangeOperation op) {
//		ProvisioningJob job = op.getProvisioningJob(new NullProgressMonitor());
//		return job.runModal(new NullProgressMonitor());
//	}

//
//	public static Set<IInstallableUnit> getPossibleUpdates() {
//		ProvisioningHelper.refreshRepositories();
//
//		IProfileRegistry registry = Provisioner.getInstance().getProfileRegistry();
//		IProfile profile = registry.getProfile(IProfileRegistry.SELF);
//		Assert.isNotNull(profile);
//		
//		IQuery<IInstallableUnit> query = QueryUtil.createIUAnyQuery();
//		IQueryResult<IInstallableUnit> units = profile.query(query, new NullProgressMonitor());
//		return units.toUnmodifiableSet();
//	}
//
//	public static IStatus updateAllFeatures() {
//		Set<IInstallableUnit> possibleUpdates = getPossibleUpdates();
//		
//		IProvisioningAgent agent = Provisioner.getInstance().getProvisioningAgent();
//		ProvisioningSession session = new ProvisioningSession(agent);
//
//		UpdateOperation updateOperation = new UpdateOperation(session, possibleUpdates);
//		Update[] possibleUpdates2 = updateOperation.getPossibleUpdates();
////		Update info.elexis.server.fhir.rest.core 1.0.0.201907120904 ==> info.elexis.server.fhir.rest.core 1.0.0.201907121055
////				Update info.elexis.server.findings.fhir.jpa.service 1.0.0.201907120904 ==> info.elexis.server.findings.fhir.jpa.service 1.0.0.201907121055
////				Update info.elexis.server.core.feature.feature.group 1.0.0.201907120904 ==> info.elexis.server.core.feature.feature.group 1.0.0.201907121055
////				Update info.elexis.server.core.jsp.feature.feature.group 1.0.0.201907120904 ==> info.elexis.server.core.jsp.feature.feature.group 1.0.0.201907121055
////				Update info.elexis.server.core.connector.elexis 1.0.0.201907120904 ==> info.elexis.server.core.connector.elexis 1.0.0.201907121055
////				Update info.elexis.server.core.p2.feature.feature.group 1.0.0.201907120904 ==> info.elexis.server.core.p2.feature.feature.group 1.0.0.201907121055
////				Update info.elexis.server.core.connector.elexis.feature.feature.group 1.0.0.201907120904 ==> info.elexis.server.core.connector.elexis.feature.feature.group 1.0.0.201907121055
////				Update at.medevit.logback.pushnotification 1.0.0.201907120904 ==> at.medevit.logback.pushnotification 1.0.0.201907121055
////				Update info.elexis.server.webapp 1.0.0.201907120904 ==> info.elexis.server.webapp 1.0.0.201907121055
////				Update info.elexis.server.core.common 1.0.0.201907120904 ==> info.elexis.server.core.common 1.0.0.201907121055
////				Update info.elexis.server.openid.feature.feature.group 1.0.0.201907120904 ==> info.elexis.server.openid.feature.feature.group 1.0.0.201907121055
////				Update info.elexis.server.core.platform.feature.feature.group 1.0.0.201907120904 ==> info.elexis.server.core.platform.feature.feature.group 1.0.0.201907121055
////				Update info.elexis.server.findings.feature.feature.group 1.0.0.201907120904 ==> info.elexis.server.findings.feature.feature.group 1.0.0.201907121055
////				Update info.elexis.server.openid 1.3.4.201907120904 ==> info.elexis.server.openid 1.3.4.201907121055
////				Update info.elexis.server.fhir.rest.core.feature.feature.group 2.0.0.201907120904 ==> info.elexis.server.fhir.rest.core.feature.feature.group 2.0.0.201907121055
//		IStatus status = updateOperation.resolveModal(new TimeoutProgressMonitor(15000));
//		if (status.getSeverity() == Status.ERROR) {
//			StatusUtil.logStatus("determine updates", log, status);
//		}
//		if ((!status.isOK() && status.getCode() == 10000 && status.getSeverity() == 1)) {
//			// no updates available
//			return Status.OK_STATUS;
//		} 
//
//		IStatus stat = ProvisioningHelper.performOperation(updateOperation);
//		StatusUtil.logStatus("update finished", log, stat);
//		return stat;
//	}
//
//	public static Collection<IInstallableUnit> getAllInstalledFeatures() {
//		IProfileRegistry registry = Provisioner.getInstance().getProfileRegistry();
//		IProfile profile = registry.getProfile(IProfileRegistry.SELF);
//		if (profile == null) {
//			return Collections.emptyList();
//		}
//		IQueryResult<IInstallableUnit> result = profile.query(QueryUtil.createIUGroupQuery(),
//				new NullProgressMonitor());
//		return result.toUnmodifiableSet();
//
//	}
//
//	/**

//
//	public static Collection<IInstallableUnit> getAllAvailableFeatures() {
//		IMetadataRepositoryManager metadataRepositoryManager = Provisioner.getInstance().getMetadataRepositoryManager();
//
//		IQueryResult<IInstallableUnit> result = metadataRepositoryManager.query(QueryUtil.createIUGroupQuery(),
//				new NullProgressMonitor());
//		return result.toSet();
//	}
//
//	private static IInstallableUnit findFeature(String feature) {
//		List<IInstallableUnit> features = new ArrayList<>(getAllAvailableFeatures());
//		// sort in order to have the newest feature on top
//		Collections.sort(features, Collections.reverseOrder());
//
//		for (Iterator<IInstallableUnit> i = features.iterator(); i.hasNext();) {
//			IInstallableUnit unit = (IInstallableUnit) i.next();
//			if (unit.getId().equals(feature))
//				return unit;
//		}
//		return null;
//	}
//
//	/**
//	 * 
//	 * @param featureName
//	 * @param install     <code>true</code> to install, <code>false</code> to
//	 *                    uninstall
//	 * @return
//	 */
//	public static String unInstallFeature(String featureName, boolean install) {
//		IInstallableUnit unit = findFeature(featureName);
//		if (unit == null) {
//			log.error(("[" + ((!install) ? "UN" : "") + "INSTALL] Cannot find feature : " + featureName));
//			return "[ERROR]";
//		}
//		return StatusUtil.printStatus(unInstall(unit, new NullProgressMonitor(), install));
//	}
//
//	public static IStatus unInstall(IInstallableUnit unit, IProgressMonitor monitor, boolean install) {
//		IProvisioningAgent agent = Provisioner.getInstance().getProvisioningAgent();
//		ProvisioningSession session = new ProvisioningSession(agent);
//
//		ProfileChangeOperation operation;
//		if (install) {
//			operation = new InstallOperation(session, Arrays.asList(unit));
//		} else {
//			operation = new UninstallOperation(session, Arrays.asList(unit));
//		}
//
//		IStatus result = operation.resolveModal(monitor);
//
//		log.info("[" + ((!install) ? "UN" : "") + "INSTALL] unit " + unit + " | result " + result.getMessage()
//				+ " | severity " + result.getSeverity() + " | code " + result.getCode());
//
//		if (result.isOK()) {
//			ProvisioningJob job = operation.getProvisioningJob(monitor);
//			job.schedule();
//			try {
//				job.join();
//				result = job.getResult();
//			} catch (InterruptedException e) {
//				return new Status(Status.ERROR, "info.elexis.server.core.p", e.getMessage());
//			}
//		}
//
//		return result;
//	}
//}
