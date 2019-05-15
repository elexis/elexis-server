package info.elexis.server.core.p2.console;

import java.util.Iterator;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import info.elexis.server.core.p2.internal.HTTPServiceHelper;
import info.elexis.server.core.p2.internal.ProvisioningHelper;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {

	public void _es_p2(CommandInterpreter ci) {
		executeCommand("es_p2", ci);
	}

	public String __executeUpdate() {
		return ProvisioningHelper.updateAllFeatures().getMessage();
	}

	public String __features() {
		return getHelp();
	}

	public String __features_listLocal() {
		return ProvisioningHelper.getAllInstalledFeatures().stream()
				.map(i -> i.getId() + " (" + i.getVersion() + ") " + i.getProperty("git-repo-url")+"  "+i.getProperty("git-rev"))
				.reduce((u, t) -> u + "\n" + t).get();
	}

	public String __features_install(Iterator<String> args) {
		if (args.hasNext()) {
			return ProvisioningHelper.unInstallFeature(args.next(), true);
		}
		return missingArgument("featureName");
	}

	public String __features_uninstall(Iterator<String> args) {
		if (args.hasNext()) {
			return ProvisioningHelper.unInstallFeature(args.next(), false);
		}
		return missingArgument("featureName");
	}

	public String __repo() {
		return getHelp();
	}

	public String __repo_list() {
		return HTTPServiceHelper.getRepoInfo(null).toString();
	}

	public String __repo_add(Iterator<String> args) {
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
			return HTTPServiceHelper.doRepositoryAdd(url, user, password).getStatusInfo().toString();
		}
		return missingArgument("url [user] [password]");
	}

	public String __repo_remove(Iterator<String> args) {
		if (args.hasNext()) {
			final String url = args.next();
			return HTTPServiceHelper.doRepositoryRemove(url).getStatusInfo().toString();
		}
		return missingArgument("url");
	}

}
