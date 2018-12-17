package info.elexis.server.core.console;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.framework.console.CommandInterpreter;

public class ConsoleProgressMonitor implements IProgressMonitor {

	private CommandInterpreter ci;
	private String name;
	private int totalWork;
	private int worked = 0;

	public ConsoleProgressMonitor(CommandInterpreter ci) {
		this.ci = ci;
	}

	@Override
	public void beginTask(String name, int totalWork) {
		this.name = name;
		this.totalWork = totalWork;
		printStatus();
	}

	private void printStatus() {
		ci.println(name + " [" + worked + "/" + totalWork + "]");
	}

	@Override
	public void done() {
		this.worked = this.totalWork;
		printStatus();
	}

	@Override
	public void internalWorked(double work) {
	}

	@Override
	public boolean isCanceled() {
		return false;
	}

	@Override
	public void setCanceled(boolean value) {
	}

	@Override
	public void setTaskName(String name) {
		this.name = name;
	}

	@Override
	public void subTask(String name) {
		this.name = name;
	}

	@Override
	public void worked(int work) {
		worked += work;
		printStatus();
	}

}
