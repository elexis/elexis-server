package info.elexis.server.core.redmine.internal.mis;

import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.LoggerFactory;

public class LicensedFeature {
	
	private String id;
	private String p2RepoUrl;
	
	public LicensedFeature(String id, String p2RepoUrl){
		this.id = id;
		this.p2RepoUrl = p2RepoUrl;
	}
	
	public String getId(){
		return id;
	}
	
	public URI getP2Url(){
		try {
			return new URI(p2RepoUrl);
		} catch (URISyntaxException e) {
			LoggerFactory.getLogger(getClass()).warn("Invalid URI [{}] in feature [{}]", p2RepoUrl,
				id, e);
		}
		return null;
	}
	
}
