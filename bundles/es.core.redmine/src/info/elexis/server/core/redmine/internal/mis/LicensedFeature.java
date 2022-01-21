package info.elexis.server.core.redmine.internal.mis;

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
	
	/**
	 * @return the repository url this feature is installable from, may contain the {{p2.branch}}
	 *         variable
	 */
	public String getP2RepoUrl(){
		return p2RepoUrl;
	}
	
}
