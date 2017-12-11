package info.elexis.server.core.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import info.elexis.server.core.Application;
import info.elexis.server.core.console.AbstractConsoleCommandProvider;
import info.elexis.server.core.security.LocalUserUtil;
import info.elexis.server.core.security.oauth2.internal.ClientAuthenticationFile;
import info.elexis.server.core.security.oauth2.internal.OAuthService;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {

	public void _es(CommandInterpreter ci) {
		executeCommand(ci);
	}

	public String __system() {
		return getHelp(1);
	}

	public String __system_halt(Iterator<String> args) {
		boolean force = false;
		if (args.hasNext()) {
			force = "force".equals(args.next());
		}
		String vetoReason = Application.shutdown(force);
		return (vetoReason != null) ? vetoReason : ok();
	}

	public String __system_restart(Iterator<String> args) {
		boolean force = false;
		if (args.hasNext()) {
			force = "force".equals(args.next());
		}
		String vetoReason = Application.restart(force);
		return (vetoReason != null) ? vetoReason : ok();
	}

	public String __system_status() {
		return Application.uptime();
	}

	public void __system_logTestError() {
		LoggerFactory.getLogger(ConsoleCommandProvider.class).error("TEST {}", "ERROR", new Throwable("Diagnosis"));
		ok();
	}

	public void __system_reloadLogConfig() {
		ConsoleCommandProvider.reloadLogger();
		ok();
	}

	public String __system_security() {
		return getHelp(2);
	}

	public String __system_security_user() {
		return getHelp(3);
	}

	public String __system_security_user_add(Iterator<String> args) throws IOException {
		if (!args.hasNext()) {
			return requireArgs("userId", "password", "roleA,roleB,roleC").toString();
		}
		String userId = args.next();
		String password = args.next();
		Set<String> roles = Arrays.asList(args.next().split(",")).stream().collect(Collectors.toSet());
		String pw = LocalUserUtil.addOrReplaceLocalUser(userId, password, roles);
		return ok("password is " + pw);
	}

	public String __system_security_user_list() {
		return LocalUserUtil.printLocalUsers();
	}

	public void __system_security_user_delete(Iterator<String> args) throws IOException {
		if (!args.hasNext()) {
			requireArgs("userId").toString();
		}
		LocalUserUtil.removeLocalUser(args.next());
	}
	
	public String __system_security_oauth() {
		return getHelp(3);
	}

	public String __system_security_oauth_addClient(Iterator<String> args) throws IOException {
		if (!args.hasNext()) {
			return requireArgs("clientId", "roleA,roleB,roleC", "[clientSecret]").toString();
		}
		String clientId = args.next();
		String clientSecret = args.next();
		Set<String> roles = Arrays.asList(args.next().split(",")).stream().collect(Collectors.toSet());
		String pw = ClientAuthenticationFile.getInstance().addOrReplaceId(clientId, roles, clientSecret);
		return ok("password is " + pw);
	}

	public String __system_security_oauth_listClients() {
		return ClientAuthenticationFile.getInstance().printEntries();
	}

	public void __system_security_oauth_deleteClient(Iterator<String> args) throws IOException {
		if (!args.hasNext()) {
			requireArgs("clientId").toString();
		}
		String clientId = args.next();
		ClientAuthenticationFile.getInstance().removeId(clientId);
	}

	public String __system_security_oauth_status() {
		return OAuthService.printStatus();
	}

	/**
	 * Reconfigures the system logger by reloading all log configuration files
	 * 
	 * @see <a href=
	 *      "http://stackoverflow.com/questions/9320133/how-do-i-programmatically
	 *      -tell-logback-to-reload-configuration">stackoverflow</a>
	 */
	public static void reloadLogger() {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		ContextInitializer ci = new ContextInitializer(loggerContext);
		URL url = ci.findURLOfDefaultConfigurationFile(true);

		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(loggerContext);
			loggerContext.reset();
			configurator.doConfigure(url);
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}
		StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
	}

}
