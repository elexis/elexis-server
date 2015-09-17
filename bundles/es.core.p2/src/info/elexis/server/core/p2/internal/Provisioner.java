package info.elexis.server.core.p2.internal;

import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(immediate = true)
public class Provisioner {

	private static Provisioner instance;

	private IProvisioningAgentProvider agentProvider;
	private IProvisioningAgent agent;

	@Reference(service = IProvisioningAgentProvider.class, 
			   cardinality = ReferenceCardinality.MANDATORY, 
			   policy = ReferencePolicy.STATIC, 
			   unbind = "unsetAgentProvider")
	protected void setAgentProvider(IProvisioningAgentProvider agentProvider) {
		this.agentProvider = agentProvider;
	}

	protected void unsetAgentProvider(IProvisioningAgentProvider agentProvider) {
		this.agentProvider = null;
	}

	@Activate
	public void activate(ComponentContext context) throws Exception {
		instance = this;
		agent = agentProvider.createAgent(null);
	}

	public static Provisioner getInstance() {
		return instance;
	}

	public IProvisioningAgent getProvisioningAgent() {
		return this.agent;
	}

	public String getCurrentProfile() {
		return "profile";
	}

	public IMetadataRepositoryManager getMetadataRepositoryManager() {
		return (IMetadataRepositoryManager) getProvisioningAgent().getService(IMetadataRepositoryManager.SERVICE_NAME);
	}

	public IArtifactRepositoryManager getArtifactRepositoryManager() {
		return (IArtifactRepositoryManager) getProvisioningAgent().getService(IArtifactRepositoryManager.SERVICE_NAME);
	}

	public IProfileRegistry getProfileRegistry() {
		return (IProfileRegistry) getProvisioningAgent().getService(IProfileRegistry.SERVICE_NAME);
	}
}
