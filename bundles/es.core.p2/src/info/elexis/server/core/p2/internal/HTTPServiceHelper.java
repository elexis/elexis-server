//package info.elexis.server.core.p2.internal;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URLDecoder;
//
//import javax.ws.rs.core.Response;
//
//import org.eclipse.core.runtime.IStatus;
//import org.eclipse.core.runtime.OperationCanceledException;
//import org.eclipse.equinox.p2.core.ProvisionException;
//import org.eclipse.equinox.p2.repository.IRepositoryManager;
//import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
//import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
//import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
//import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class HTTPServiceHelper {
//
//	private static Logger log = LoggerFactory.getLogger(HTTPServiceHelper.class);
//
//	public static RepoInfo getRepoInfo(String filterStr) {
//		
//	}
//
//	public static Response doRepositoryList(String filterStr) {
//		try {
//			RepoInfo info = getRepoInfo(filterStr);
//			return Response.ok(info).build();
//		} catch (IllegalArgumentException e) {
//			return Response.status(Response.Status.BAD_REQUEST).build();
//		}
//	}
//
//	public static Response doRepositoryAdd(String locStr, String username, String password) {
//		URI location = null;
//		try {
//			locStr = URLDecoder.decode(locStr, "ASCII");
//			location = new URI(locStr);
//		} catch (URISyntaxException | UnsupportedEncodingException e) {
//			log.warn("Exception parsing URI " + locStr, e);
//			return Response.status(Response.Status.BAD_REQUEST).build();
//		}
//
//		if (location.isAbsolute()) {
//			ProvisioningHelper.addRepository(location, username, password);
//			return Response.ok().build();
//		}
//
//		log.warn("Tried to add non absolute location: {}", location);
//		return Response.status(Response.Status.BAD_REQUEST).build();
//	}
//
//	public static Response doRepositoryRemove(String locStr) {
//		URI location = null;
//		try {
//			locStr = URLDecoder.decode(locStr, "ASCII");
//			location = new URI(locStr);
//		} catch (URISyntaxException | UnsupportedEncodingException e) {
//			log.warn("Exception parsing URI " + locStr, e);
//			return Response.status(Response.Status.BAD_REQUEST).build();
//		}
//
//		boolean result = ProvisioningHelper.removeRepository(location);
//		if (result) {
//			return Response.ok().build();
//		}
//
//		return Response.serverError().build();
//	}
//
//	public static Response doUpdateAllFeatures() {
//		IStatus updateStatus = ProvisioningHelper.updateAllFeatures();
//		return createResponseFromStatus("doUpdateAllFeatures", updateStatus);
//	}
//
//	private static Response createResponseFromStatus(String op, IStatus stat) {
//		if (stat.isOK()) {
//			return Response.ok().build();
//		}
//		log.warn("Error performing operation [{}] : Code {} / {}", op, stat.getCode(), stat.getMessage());
//		return Response.serverError().build();
//	}
//
//}
