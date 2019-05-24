package info.elexis.server.core.p2.console;

import java.util.Iterator;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.console.CmdAdvisor;
import info.elexis.server.core.p2.internal.HTTPServiceHelper;
import info.elexis.server.core.p2.internal.ProvisioningHelper;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {
	
	@Activate
	public void activate(){
		register(this.getClass());
	}
	
	@CmdAdvisor(description = "feature de-/installation and update repository management")
	public void _p2(CommandInterpreter ci){
		executeCommand("p2", ci);
	}
	
	@CmdAdvisor(description = "update all installed features")
	public String __p2_executeUpdate(){
		return ProvisioningHelper.updateAllFeatures().getMessage();
	}
	
	@CmdAdvisor(description = "list the installed features")
	public String __p2_features_listLocal(){
		return ProvisioningHelper
			.getAllInstalledFeatures().stream().map(i -> i.getId() + " (" + i.getVersion() + ") "
				+ i.getProperty("git-repo-url") + "  " + i.getProperty("git-rev"))
			.reduce((u, t) -> u + "\n" + t).get();
	}
	
	@CmdAdvisor(description = "install a feature")
	public String __p2_features_install(Iterator<String> args){
		if (args.hasNext()) {
			return ProvisioningHelper.unInstallFeature(args.next(), true);
		}
		return missingArgument("featureName");
	}
	
	@CmdAdvisor(description = "uninstall a feature")
	public String __p2_features_uninstall(Iterator<String> args){
		if (args.hasNext()) {
			return ProvisioningHelper.unInstallFeature(args.next(), false);
		}
		return missingArgument("featureName");
	}
	
	@CmdAdvisor(description = "list configured repositories")
	public String __p2_repo_list(){
		return HTTPServiceHelper.getRepoInfo(null).toString();
	}
	
	public String __p2_repo_add(Iterator<String> args){
		if (args.hasNext()) {
			final String url = args.next();
			String user = null;
			String password = null;
			if (args.hasNext()) {
				user = args.next();
			}
			if (args.hasNext()) {
				password = args.next();
			}
			return HTTPServiceHelper.doRepositoryAdd(url, user, password).getStatusInfo()
				.toString();
		}
		return missingArgument("url [user] [password]");
	}
	
	public String __p2_repo_remove(Iterator<String> args){
		if (args.hasNext()) {
			final String url = args.next();
			return HTTPServiceHelper.doRepositoryRemove(url).getStatusInfo().toString();
		}
		return missingArgument("url");
	}
	
}
