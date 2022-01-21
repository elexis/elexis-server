package info.elexis.server.core.redmine.internal.mis;

import java.net.Authenticator;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.internal.ResultsWrapper;

import ch.elexis.core.console.ConsoleProgressMonitor;
import ch.elexis.core.status.StatusUtil;
import info.elexis.server.core.Application;
import info.elexis.server.core.p2.IProvisioner;
import info.elexis.server.core.redmine.internal.Constants;

/**
 * Provide automated install of licensed features for customers of Medelexis. If no respective api
 * key is provided this will do nothing.
 */
@Component(service = {}, immediate = true)
public class MedelexisFeatureManagement {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Reference
	private IProvisioner provisioner;
	
	@Activate
	public void activate(){
		
		if (getMisApiKey() == null) {
			logger.info("env var [{}] not provided - skipping p2 feature management",
				Constants.ENV_VAR_MIS_API_KEY);
			return;
		}
		
		// register authenticator for *.medelexis.ch
		Authenticator makba =
			new MisApiKeyBasicAuthenticator(getP2RepoUsername(), getMisApiKey().toCharArray());
		Authenticator.setDefault(makba);
		
		boolean requiresReboot = false;
		
		try {
			List<LicensedFeature> licensedFeatures = getLicensedFeatures();
			if (licensedFeatures.isEmpty()) {
				logger.warn("No licensed features found");
				return;
			}
			
			Collection<IInstallableUnit> installedFeatures = provisioner.getInstalledFeatures();
			if (installedFeatures.isEmpty()) {
				logger.warn("installedFeatures is empty");
			}
			HashMap<String, IInstallableUnit> installedFeaturesMap = new HashMap<>();
			for (IInstallableUnit unit : installedFeatures) {
				installedFeaturesMap.put(unit.getId(), unit);
			}
			
			List<IInstallableUnit> toInstall = new ArrayList<>();
			for (LicensedFeature licensedFeature : licensedFeatures) {
				IInstallableUnit iu = installedFeaturesMap.get(licensedFeature.getId());
				if (iu == null) {
					
					URI p2repoUrl = replaceP2BranchVariable(licensedFeature.getP2RepoUrl());
					if(p2repoUrl == null) {
						logger.warn("Invalid repo url, continuing.");
						continue;
					}
					
					logger.info("Preparing installation for licensed feature [{}] from [{}]",
						licensedFeature.getId(), p2repoUrl);
					
					provisioner.addRepository(p2repoUrl, getP2RepoUsername(), getMisApiKey());
					IStatus loadRepositoryStatus =
						provisioner.loadRepository(new ConsoleProgressMonitor(), p2repoUrl);
					StatusUtil.logStatus(logger, loadRepositoryStatus);
					
					IInstallableUnit iuToInstall = provisioner.getFeatureInAllAvailableFeatures(
						new ConsoleProgressMonitor(), licensedFeature.getId());
					if (iuToInstall == null) {
						logger.warn("Could not find feature [{}] in available repositories",
							licensedFeature.getId());
					} else {
						toInstall.add(iuToInstall);
					}
				}
			}
			
			// TODO automated uninstall
			
			for (IInstallableUnit toInstallUnit : toInstall) {
				logger.info("Installing licensed feature [{}]", toInstallUnit.getId());
				IStatus status = provisioner.install(toInstallUnit, new ConsoleProgressMonitor());
				StatusUtil.logStatus(logger, status, true);
				requiresReboot = true;
			}
			
		} catch (Exception e) {
			logger.warn("Error in feature management", e);
		}
		
		if (requiresReboot) {
			logger.info("Reboot required");
			Application.restart(false);
		}
		
	}
	
	private URI replaceP2BranchVariable(String p2RepoUrl){
		String _url = p2RepoUrl.replaceAll("\\{\\{p2.branch\\}\\}", getElexisBranch());
		try {
			return new URI(_url);
		} catch (URISyntaxException e) {
			logger.warn("Error resolving url []", _url);
			return null;
		}
	}
	
	List<LicensedFeature> getLicensedFeatures() throws RedmineException{
		RedmineManager redmineManager =
			RedmineManagerFactory.createWithApiKey(getMisBaseUrl(), getMisApiKey());
		Map<String, String> params = new HashMap<>();
		params.put("offset", "0");
		params.put("limit", "100");
		params.put("project_id", getMisProjectId());
		params.put("tracker_id", getServiceESTrackerId());
		params.put("status_id", "open");
		
		ResultsWrapper<Issue> issues = redmineManager.getIssueManager().getIssues(params);
		if (issues.getTotalFoundOnServer() > 100) {
			// TODO implement paging
			logger.warn("More than 100 issue found - paging not implemented!");
		}
		List<Issue> results = issues.getResults();
		List<LicensedFeature> licensedFeatures = new ArrayList<>();
		for (Issue issue : results) {
			CustomField customFieldByName = issue.getCustomFieldByName("p2RepoUrl");
			CustomField aboState = issue.getCustomFieldByName("Abostatus");

			if("EXPIRED".equalsIgnoreCase(aboState.getValue())) {
				logger.info("[{}] is EXPIRED, skipping installation.", issue.getSubject());
				continue;
			}
			
			licensedFeatures
				.add(new LicensedFeature(issue.getSubject(), customFieldByName.getValue()));
		}
		return licensedFeatures;
	}
	
	private String getServiceESTrackerId(){
		String overrideTrackerId = System.getenv("mis.trackerId");
		if (overrideTrackerId != null) {
			logger.info("Override tracker.id = " + overrideTrackerId);
			return overrideTrackerId;
		}
		return "14";
	}
	
	private String getMisBaseUrl(){
		String overrideMisUrl = System.getenv("mis.Url");
		if (overrideMisUrl != null) {
			logger.info("Override mis.url = " + overrideMisUrl);
			return overrideMisUrl;
		}
		return "https://mis.medelexis.ch";
	}
	
	private String getP2RepoUsername(){
		return DigestUtils.md5Hex(getMisApiKey());
	}
	
	String getElexisBranch(){
		return System.getenv(Constants.ENV_VAR_ELEXIS_BRANCH);
	}
	
	String getMisApiKey(){
		return System.getenv(Constants.ENV_VAR_MIS_API_KEY);
	}
	
	String getMisProjectId(){
		return System.getenv(Constants.ENV_VAR_MIS_PROJECTID);
	}
}
