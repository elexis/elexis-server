package info.elexis.server.core.connector.elexis.internal;

import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.eclipse.osgi.framework.console.CommandProvider;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.console.AbstractConsoleCommandProvider;
import ch.elexis.core.console.CmdAdvisor;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IMessageService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.core.connector.elexis.common.ElexisDBConnection;
import info.elexis.server.core.connector.elexis.instances.InstanceService;
import info.elexis.server.core.connector.elexis.internal.services.LogEventHandler;
import info.elexis.server.core.connector.elexis.locking.ILockServiceContributor;
import info.elexis.server.core.connector.elexis.locking.LogLockServiceContributor;
import info.elexis.server.core.connector.elexis.services.LockService;

@Component(service = CommandProvider.class, immediate = true)
public class ConsoleCommandProvider extends AbstractConsoleCommandProvider {
	
	@Reference
	private IContextService contextService;

	private ServiceRegistration<ILockServiceContributor> logLockService;
	private ServiceRegistration<EventHandler> logEventHandler;
	
	
	@Activate
	public void activate(){
		register(this.getClass());
	}
	
	@CmdAdvisor(description = "elexis database connector")
	public void _elc(CommandInterpreter ci){
		executeCommand("elc", ci);
	}
	
	@CmdAdvisor(description = "show database connection and status information")
	public String __elc_status(){
		StringBuilder sb = new StringBuilder();
		sb.append("DB:\t\t" + ElexisDBConnection.getDatabaseInformationString() + "\n");
		sb.append("LS UUID:\t[" + LockService.getSystemuuid() + "]\n");
		sb.append("StationId:\t" + contextService.getStationIdentifier() + "\n");
		sb.append("Default-TZ:\t"+ TimeZone.getDefault().getID()+"\n");
		sb.append("Locks:");
		for (LockInfo lockInfo : LockService.getAllLockInfo()) {
			sb.append("\t\t" + lockInfo.getUser() + "@" + lockInfo.getElementType() + "::"
				+ lockInfo.getElementId() + "\t" + lockInfo.getCreationDate() + "\t["
				+ lockInfo.getSystemUuid() + "]\n");
		}
		return sb.toString();
	}
	
	@CmdAdvisor(description = "enable elexis event logging, optional topic parameter")
	public void __elc_eventlog_enable(String topic){
		if (topic == null) {
			topic = ElexisEventTopics.BASE + "*";
		}
		
		if (logEventHandler == null) {
			Dictionary<String, Object> properties = new Hashtable<>();
			properties.put(EventConstants.EVENT_TOPIC, topic);
			logEventHandler = Activator.getContext().registerService(EventHandler.class,
				new LogEventHandler(), properties);
			ok(logEventHandler.getReference());
		}
	}
	
	@CmdAdvisor(description = "disable elexis event logging")
	public void __elc_eventlog_disable() {
		if (logEventHandler != null) {
			logEventHandler.unregister();
			logEventHandler = null;
			ok("unregistered");
		}
	}
	
	@CmdAdvisor(description = "send a message to a given user")
	public void __elc_message(List<String> args){
		if (args.isEmpty()) {
			fail("usage: elc message userid message");
		}
		
		Optional<IMessageService> messageService =
			OsgiServiceUtil.getService(IMessageService.class);
		if (messageService.isPresent()) {
			TransientMessage message =
				messageService.get().prepare(contextService.getStationIdentifier(),
					IMessageService.INTERNAL_MESSAGE_URI_SCHEME + ":" + args.get(0));
			message.setMessageText(args.get(1));
			ObjectStatus status = messageService.get().send(message);
			ok(status);
		}
		fail("messageService not found");
	}
	
	@CmdAdvisor(description = "list all elexis instances connected to this server instance")
	public void __elc_listInstances(){
		List<InstanceStatus> status = InstanceService.getInstanceStatus();
		for (int i = 0; i < status.size(); i++) {
			InstanceStatus inst = status.get(i);
			ci.println(i + ") " + inst.getRemoteAddress() + " " + inst);
			long until = new Date().getTime() - inst.getLastUpdate().getTime();
			ci.print("\tFS:" + inst.getFirstSeen() + " LU:" + inst.getLastUpdate());
			if (until > 60 * 1000) {
				ci.print(" (!!!!)\n");
			} else {
				ci.print("\n");
			}
		}
	}
	
	@CmdAdvisor(description = "clear the list of active elexis instances held by this server")
	public String __elc_listInstances_clear(){
		InstanceService.clearInstanceStatus();
		return ok();
	}
	
	@CmdAdvisor(description = "list all locks held by this server")
	public void __elc_locks_list(){
		for (LockInfo lockInfo : LockService.getAllLockInfo()) {
			ci.println(lockInfo.getUser() + "@" + lockInfo.getElementType() + "::"
				+ lockInfo.getElementId() + "\t" + lockInfo.getCreationDate() + "\t["
				+ lockInfo.getSystemUuid() + "]");
		}
	}
	

	
	@CmdAdvisor(description = "enable lock request logging")
	public void __elc_locks_log_enable() throws InvalidSyntaxException{
		if (logLockService == null) {
			logLockService = Activator.getContext().registerService(ILockServiceContributor.class,
				new LogLockServiceContributor(), null);
			ok();
		}
	}
	
	@CmdAdvisor(description = "disable lock request logging")
	public void __elc_locks_log_disable() throws InvalidSyntaxException{
		if (logLockService != null) {
			logLockService.unregister();
			logLockService = null;
			ok();
		}
	}
	
	@CmdAdvisor(description = "clear all locks held by this server")
	public void __elc_locks_clearAll(){
		LockService.clearAllLocks();
		ok();
	}
	
	@CmdAdvisor(description = "clear a single lock held by this server")
	public String __elc_locks_clearSingle(String elementId){
		if (elementId != null) {
			return Boolean.toString(LockService.clearLock(elementId));
		} else {
			return missingArgument("elementId");
		}
	}
	
	@CmdAdvisor(description = "list all configuration entries (optional key argument)")
	public void __elc_config_list(Iterator<String> args){
		String nodePrefix = args.next();
		if (StringUtils.isEmpty(nodePrefix)) {
			nodePrefix = null;
		}
		
		IQuery<IConfig> qre = CoreModelServiceHolder.get().getQuery(IConfig.class);
		if (nodePrefix != null) {
			qre.and(ModelPackage.Literals.ICONFIG__KEY, COMPARATOR.LIKE, nodePrefix + "%");
		}
		List<IConfig> nodes = qre.execute();
		if (nodes.size() == 1) {
			ci.println("Value: " + nodes.get(0).getValue());
		} else {
			prflp("Key", 50);
			prflp("Value", 50);
			prflp("LastUpdate", 25, true);
			for (IConfig config : nodes) {
				prflp(config.getKey(), 50);
				prflp(config.getValue(), 50);
				prflp(TimeUtil.formatSafe(config.getLastupdate()), 25, true);
			}
		}
	}
	
	@CmdAdvisor(description = "set (add or overwrite) a global configuration entry: key value|(null:remove)")
	public void __elc_config_set(String key, String value){
		if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
			missingArgument("key value|null");
			return;
		}
		
		boolean remove = "null".equalsIgnoreCase(value);
		
		IConfig config = CoreModelServiceHolder.get().load(key, IConfig.class).orElse(null);
		if (config == null) {
			if (remove) {
				ok("remove");
				return;
			}
			config = CoreModelServiceHolder.get().create(IConfig.class);
			config.setKey(key);
		}
		if (remove) {
			CoreModelServiceHolder.get().remove(config);
			ok("remove");
			return;
		}
		config.setValue(value);
		CoreModelServiceHolder.get().save(config);
		ok(config);
	}
	
}
