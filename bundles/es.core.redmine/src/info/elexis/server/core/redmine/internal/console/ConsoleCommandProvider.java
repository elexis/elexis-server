package info.elexis.server.core.redmine.internal.console;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import com.taskadapter.redmineapi.RedmineException;

import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.console.CmdAdvisor;
import info.elexis.server.core.redmine.internal.RedmineUtil;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {
	
	@Activate
	public void activate(){
		register(this.getClass());
	}
	
	@CmdAdvisor(description = "redmine utils")
	public void _redmine(CommandInterpreter ci){
		executeCommand("redmine", ci);
	}
	
	@CmdAdvisor(description = "append current log (max 1mb) to an (optional) issue number")
	public String __redmine_sendlog(String issueNumber) throws RedmineException, IOException{
		Integer issueId = (StringUtils.isNumeric(issueNumber)) ? Integer.parseInt(issueNumber) : null;
		return RedmineUtil.INSTANCE.sendLogToRedmine(issueId);
	}
	
}
