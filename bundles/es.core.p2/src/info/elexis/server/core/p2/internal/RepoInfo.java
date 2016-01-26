package info.elexis.server.core.p2.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class RepoInfo {
	@XmlElement
	public List<RepoElement> metadataRepos = new ArrayList<RepoInfo.RepoElement>();
	@XmlElement
	public List<RepoElement> artifactRepos = new ArrayList<RepoInfo.RepoElement>();

	public void addMetadataRepoElement(String repoName, URI repoLoc) {
		metadataRepos.add(new RepoElement(repoName, repoLoc));
	}

	public void addArtifactRepoElement(String repoName, URI repoLoc) {
		artifactRepos.add(new RepoElement(repoName, repoLoc));
	}

	public static class RepoElement {
		private String name;
		private URI uri;

		public RepoElement(String repoName, URI repoLoc) {
			name = repoName;
			uri = repoLoc;
		}

		public String getName() {
			return this.name;
		}

		public URI getURI() {
			return this.uri;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setURI(URI uri) {
			this.uri = uri;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("- MetaData Repositories ------------\n");
		for (RepoElement re : metadataRepos) {
			sb.append("\t"+re.name+"\t"+re.uri+"\n");
		}
		sb.append("- Artifact Repositories ------------\n");
		for (RepoElement re : artifactRepos) {
			sb.append("\t"+re.name+"\t"+re.uri+"\n");
		}
		sb.append("------------------------------------\n");
		return sb.toString();
	}
	
}
