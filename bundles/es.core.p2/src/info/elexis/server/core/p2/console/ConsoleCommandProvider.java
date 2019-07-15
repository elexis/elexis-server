package info.elexis.server.core.p2.console;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.Update;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.console.CmdAdvisor;
import ch.elexis.core.console.ConsoleProgressMonitor;
import ch.elexis.core.status.StatusUtil;
import info.elexis.server.core.p2.IProvisioner;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {

	@Reference
	private IProvisioner provisioner;

	@Activate
	public void activate() {
		register(this.getClass());
	}

	@CmdAdvisor(description = "feature de-/installation and update repository management")
	public void _p2(CommandInterpreter ci) {
		executeCommand("p2", ci);
	}

	@CmdAdvisor(description = "list possible updates")
	public void __p2_update_list() {
		Collection<Update> availableUpdates = provisioner.getAvailableUpdates();
		availableUpdates.stream().forEach(c -> ci.println(c));
	}

	@CmdAdvisor(description = "update all installed features")
	public String __p2_update_execute() {
		Collection<Update> availableUpdates = provisioner.getAvailableUpdates();
		IStatus update = provisioner.update(availableUpdates, new ConsoleProgressMonitor(ci));
		return StatusUtil.printStatus(update);
	}

	@CmdAdvisor(description = "list the installed features")
	public String __p2_features_list() {
		Collection<IInstallableUnit> installedFeatures = provisioner.getInstalledFeatures();
		Optional<String> reduce = installedFeatures.stream().map(i -> i.getId() + " (" + i.getVersion() + ") "
				+ i.getProperty("git-repo-url") + "  " + i.getProperty("git-rev")).reduce((u, t) -> u + "\n" + t);
		return reduce.orElse("fail");
	}

	@CmdAdvisor(description = "install a feature")
	public String __p2_features_install(Iterator<String> args) {
		if (args.hasNext()) {
			IStatus status = provisioner.install(args.next(), new ConsoleProgressMonitor(ci));
			return StatusUtil.printStatus(status);
		}
		return missingArgument("featureName");
	}

	@CmdAdvisor(description = "uninstall a feature")
	public String __p2_features_uninstall(Iterator<String> args) {
		if (args.hasNext()) {
			IStatus status = provisioner.uninstall(args.next(), new ConsoleProgressMonitor(ci));
			return StatusUtil.printStatus(status);
		}
		return missingArgument("featureName");
	}

	@CmdAdvisor(description = "list configured repositories")
	public String __p2_repo_list() {
		return provisioner.getRepositoryInfo().toString();
	}

	@CmdAdvisor(description = "add a repository")
	public String __p2_repo_add(Iterator<String> args) {
		if (args.hasNext()) {
			final String _url = args.next();
			URI uri;
			try {
				URL url = new URL(_url);
				uri = url.toURI();
			} catch (MalformedURLException | URISyntaxException e) {
				return e.getMessage();
			}
			String username = null;
			String password = null;
			if (args.hasNext()) {
				username = args.next();
			}
			if (args.hasNext()) {
				password = args.next();
			}
			provisioner.addRepository(uri, username, password);
			return ok();
		}
		return missingArgument("url [user] [password]");
	}

	@CmdAdvisor(description = "remove a repository")
	public String __p2_repo_remove(Iterator<String> args) {
		if (args.hasNext()) {
			final String _url = args.next();
			URI uri;
			try {
				URL url = new URL(_url);
				uri = url.toURI();
			} catch (MalformedURLException | URISyntaxException e) {
				return e.getMessage();
			}
			boolean success = provisioner.removeRepository(uri);
			if (success) {
				return ok();
			}
		}
		return "fail";
	}

}
