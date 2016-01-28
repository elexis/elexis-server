package info.elexis.server.core.p2.internal;

import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;

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
}
