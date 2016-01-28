package info.elexis.server.core.common;

import java.io.PrintStream;

import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;

// TODO refactor ...
public class StatusUtil {
	public static void printStatus(Logger log, IStatus status) {
		print(log, "", status);
	}

	private static void print(Logger log, String indent, IStatus status) {
		if (status.isMultiStatus()) {
			log.warn(indent + status.getMessage().replace('\n', ' '));
			String childIndent = indent + "  ";
			for (IStatus c : status.getChildren()) {
				print(log, childIndent, c);
			}
		} else {
			log.warn(indent + status.getMessage().replace('\n', ' '));
		}
	}

	public static void printStatus(PrintStream out, IStatus status) {
		print(out, "", status);
	}

	private static void print(PrintStream log, String indent, IStatus status) {
		if (status.isMultiStatus()) {
			log.print(indent + status.getMessage());
			String childIndent = indent + "  ";
			for (IStatus c : status.getChildren()) {
				print(log, childIndent, c);
			}
		} else {
			log.println(indent + status.getMessage());
		}
	}
}
