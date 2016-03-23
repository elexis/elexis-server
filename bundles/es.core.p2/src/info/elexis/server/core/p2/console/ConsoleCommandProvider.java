package info.elexis.server.core.p2.console;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.p2.internal.HTTPServiceHelper;
import info.elexis.server.core.p2.internal.ProvisioningHelper;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider implements CommandProvider {

	public static final String MAIN_CMD = "es_p2";
	public static final String HELP_PREPEND = "Usage: " + MAIN_CMD + " ";

	private Logger log = LoggerFactory.getLogger(ConsoleCommandProvider.class);

	public void _es_p2(CommandInterpreter ci) {
		final String argument = ci.nextArgument();
		try {
			if (argument == null) {
				System.out.println(getHelp());
				return;
			}

			switch (argument.toLowerCase()) {
			case "repo":
				System.out.println(repo(ci));
				return;
			case "features":
				System.out.println(features(ci));
			case "executeUpdate":
				System.out.println(ProvisioningHelper.updateAllFeatures().getMessage());
				return;
			default:
				System.out.println(getHelp());
				return;
			}
		} catch (Exception e) {
			log.error("Execution error on argument " + argument, e);
		}
	}

	private String features(final CommandInterpreter ci) {
		final String argument = ci.nextArgument();
		if (argument == null) {
			return HELP_PREPEND + "features (listLocal | install | uninstall)";
		}

		final String featureName = ci.nextArgument();

		switch (argument) {
		case "listLocal":
			return ProvisioningHelper.getAllInstalledFeatures().stream()
					.map(i -> i.getId() + " (" + i.getVersion() + ")").reduce((u, t) -> u + "\n" + t).get();
		case "install":
			return ProvisioningHelper.installFeature(featureName);
		case "uninstall":
			return ProvisioningHelper.uninstallFeature(featureName);
		}

		return getHelp();
	}

	private String repo(final CommandInterpreter ci) {
		final String argument = ci.nextArgument();
		if (argument == null) {
			return HELP_PREPEND + "repo (list | add | remove )";
		}
		switch (argument) {
		case "list":
			return HTTPServiceHelper.getRepoInfo(null).toString();
		case "add":
			return repoManage(true, ci);
		case "remove":
			return repoManage(false, ci);
		default:
			break;
		}
		return getHelp();
	}

	private String repoManage(boolean b, CommandInterpreter ci) {
		final String argument = ci.nextArgument();
		if (argument == null) {
			return HELP_PREPEND + "repo (list | add ) repo_url [username] [password]";
		}
		final String username = ci.nextArgument();
		final String password = ci.nextArgument();

		if (b) {
			return HTTPServiceHelper.doRepositoryAdd(argument, username, password).getStatusInfo().toString();
		} else {
			return HTTPServiceHelper.doRepositoryRemove(argument).getStatusInfo().toString();
		}
	}

	@Override
	public String getHelp() {
		return HELP_PREPEND + "(repo | features | executeUpdate)";
	}

}
