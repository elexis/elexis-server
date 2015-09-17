package info.elexis.server.core.p2.internal;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.repository.IRepositoryManager;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPServiceHelper {

	private static Logger log = LoggerFactory.getLogger(HTTPServiceHelper.class);

	public static Response doRepositoryList(String filterStr) {
		int filter = IRepositoryManager.REPOSITORIES_ALL;
		try {
			if (filterStr != null) {
				filter = Integer.parseInt(filterStr);
			}
		} catch (NumberFormatException localException) {
			log.error("NaN " + filterStr, localException);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		RepoInfo info = new RepoInfo();
		IMetadataRepositoryManager metadataRepoMgr = Provisioner.getInstance().getMetadataRepositoryManager();
		IMetadataRepository repo;
		for (URI repoLoc : metadataRepoMgr.getKnownRepositories(filter)) {
			String repoName;
			try {
				repo = metadataRepoMgr.loadRepository(repoLoc, new TimeoutProgressMonitor(2000));
				repoName = repo.getName();
			} catch (OperationCanceledException localOperationCanceledException) {
				log.warn("timeout", localOperationCanceledException);
				repoName = "timeout";
			} catch (ProvisionException localProvisionException) {
				log.error("FAIL", localProvisionException);
				repoName = "FAILED: " + localProvisionException.getLocalizedMessage();
			}
			info.addMetadataRepoElement(repoName, repoLoc);
		}

		IArtifactRepositoryManager articaftRepoMgr = Provisioner.getInstance().getArtifactRepositoryManager();
		IArtifactRepository artRepo;
		for (URI repoLoc : articaftRepoMgr.getKnownRepositories(filter)) {
			String repoName;
			try {
				artRepo = articaftRepoMgr.loadRepository(repoLoc, new TimeoutProgressMonitor(2000));
				repoName = artRepo.getName();
			} catch (OperationCanceledException localOperationCanceledException) {
				log.warn("timeout", localOperationCanceledException);
				repoName = "timeout";
			} catch (ProvisionException localProvisionException) {
				log.error("FAIL", localProvisionException);
				repoName = "FAILED: " + localProvisionException.getLocalizedMessage();
			}
			info.addArtifactRepoElement(repoName, repoLoc);
		}

		return Response.ok(info).build();
	}

	public static Response doRepositoryAdd(String locStr) {
		URI location = null;
		try {
			locStr = URLDecoder.decode(locStr, "ASCII");
			location = new URI(locStr);
		} catch (URISyntaxException | UnsupportedEncodingException e) {
			log.error("Exception parsing URI " + locStr, e);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		if (location.isAbsolute()) {
			ProvisioningHelper.addRepository(location);
			return Response.ok().build();
		}

		log.warn("Tried to add non absolute location: {}", location);
		return Response.status(Response.Status.BAD_REQUEST).build();
	}

	public static Response doRepositoryRemove(String locStr) {
		URI location = null;
		try {
			locStr = URLDecoder.decode(locStr, "ASCII");
			location = new URI(locStr);
		} catch (URISyntaxException | UnsupportedEncodingException e) {
			log.error("Exception parsing URI " + locStr, e);
			return Response.status(Response.Status.BAD_REQUEST).build();
		}

		boolean result = ProvisioningHelper.removeRepository(location);
		if (result) {
			return Response.ok().build();
		}

		return Response.serverError().build();
	}

	public static Response doUpdateAllFeatures() {
		IStatus updateStatus = ProvisioningHelper.updateAllFeatures();
		return createResponseFromStatus("doUpdateAllFeatures", updateStatus);
	}

	private static Response createResponseFromStatus(String op, IStatus stat) {
		if (stat.isOK()) {
			return Response.ok().build();
		}
		log.error("Error performing operation [{}] : Code {} / {}", op, stat.getCode(), stat.getMessage());
		return Response.serverError().build();
	}

}
