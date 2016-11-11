package info.elexis.server.core.p2.console;

import java.util.Iterator;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.console.AbstractConsoleCommandProvider;
import info.elexis.server.core.p2.internal.HTTPServiceHelper;
import info.elexis.server.core.p2.internal.ProvisioningHelper;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {

	public void _es_p2(CommandInterpreter ci) {
		executeCommand(ci);
	}

	public String __executeUpdate() {
		return ProvisioningHelper.updateAllFeatures().getMessage();
	}

	public String __features() {
		return getHelp(1);
	}

	public String __features_listLocal() {
		return ProvisioningHelper.getAllInstalledFeatures().stream().map(i -> i.getId() + " (" + i.getVersion() + ")")
				.reduce((u, t) -> u + "\n" + t).get();
	}

	public String __features_install(Iterator<String> args) {
		if (args.hasNext()) {
			return ProvisioningHelper.installFeature(args.next());
		}
		return missingArgument("featureName");
	}

	public String __features_uninstall(Iterator<String> args) {
		if (args.hasNext()) {
			return ProvisioningHelper.uninstallFeature(args.next());
		}
		return missingArgument("featureName");
	}

	public String __repo_list() {
		return HTTPServiceHelper.getRepoInfo(null).toString();
	}

	public String __repo_add(Iterator<String> args) {
		if (args.hasNext()) {
			final String url = args.next();
			final String user = args.next();
			final String password = args.next();
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
