package info.elexis.server.core.console;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.internal.ConsoleCommandProvider;

public abstract class AbstractConsoleCommandProvider implements CommandProvider {

	public Logger log = LoggerFactory.getLogger(ConsoleCommandProvider.class);

	private Map<String, Method> methods;
	private String[] arguments;
	public CommandInterpreter ci;

	private String[] subArguments;

	public String getArgument(int i) {
		if (arguments.length >= i + 1) {
			return arguments[i];
		}
		return null;
	}

	public void executeCommand(CommandInterpreter ci) {
		this.ci = ci;
		String argument;
		List<String> argumentQ = new ArrayList<String>();
		while ((argument = ci.nextArgument()) != null) {
			argumentQ.add(argument);
		}
		arguments = argumentQ.toArray(new String[] {});
		subArguments = new String[] {};

		methods = new HashMap<String, Method>();
		for (Method method : this.getClass().getMethods()) {
			if (method.getName().startsWith("_")) {
				methods.put(method.getName(), method);
			}
		}

		if (arguments.length == 0) {
			ci.println(getHelp());
			return;
		}

		Method method = null;
		int counter = 0;
		StringBuilder methodName = new StringBuilder("_");
		for (int i = 0; i < arguments.length; i++) {
			methodName.append("_" + arguments[i]);
			if (methods.get(methodName.toString()) != null) {
				counter++;
				method = methods.get(methodName.toString());
			}
		}

		if (method != null) {
			try {
				Object result = null;
				String joinedArguments = String.join(" ", arguments);
				ci.println("--( " + new Date() + " )---[cmd: " + joinedArguments + "]"
						+ String.join("", Collections.nCopies(100 - joinedArguments.length(), "-")));
				if (method.getParameterCount() > 0) {
					subArguments = Arrays.copyOfRange(arguments, counter, arguments.length);
					result = method.invoke(this, Arrays.asList(subArguments).iterator());
				} else {
					result = method.invoke(this);
				}
				if (result != null && result instanceof String) {
					ci.println(result);
				}
			} catch (Exception e) {
				ci.println("Execution error on argument: " + e.getMessage());
				log.warn("Execution error on argument [{}]: ", arguments, e);
			}
		}
	}

	public String[] requireArgs(String... required) {
		int length = required.length;
		if (subArguments.length >= length) {
			return Arrays.copyOfRange(subArguments, 0, length);
		} else {
			ci.print(missingArgument(String.join(" ", required)));
		}

		return null;
	}

	public String ok() {
		return "OK";
	}

	public String missingArgument(String string) {
		return "Missing argument: " + string;
	}

	@Override
	public String getHelp() {
		return getHelp(0);
	}

	public String getHelp(int level) {
		List<String> parameters = new ArrayList<String>();
		String root = "";
		for (String key : methods.keySet()) {
			if (key.startsWith("__")) {
				String[] splitMethodNames = key.substring(2).split("_");
				if (level == 0) {
					if (splitMethodNames.length == 1) {
						parameters.add(splitMethodNames[0]);
					}
				} else if (level == 1) {
					if (arguments[0].equalsIgnoreCase(splitMethodNames[0]) && splitMethodNames.length == 2) {
						parameters.add(splitMethodNames[1]);
					}
				}
			} else if (key.startsWith("_")) {
				root = key.substring(1);
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("Usage: " + root + " ");
		for (int i = 0; i < level; i++) {
			sb.append(arguments[i]);
			if (i < level) {
				sb.append(" ");
			}
		}
		sb.append("(" + parameters.stream().sorted(Comparator.naturalOrder()).reduce((u, t) -> u + " | " + t).get()
				+ ")");

		return sb.toString();
	}

}
