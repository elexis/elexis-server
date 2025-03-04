package info.elexis.server.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;
import jakarta.activation.CommandMap;
import jakarta.activation.MailcapCommandMap;

public class ClassloadingLoggerContextListener implements  LoggerContextListener {

	private boolean initialized = false;
	
	@Override
	public boolean isResetResistant() {
		return false;
	}

	@Override
	public void onStart(LoggerContext context) {
		// add handlers for main mail MIME types
		
	}

	@Override
	public void onReset(LoggerContext context) {
		System.out.println("onReste");
	}

	@Override
	public void onStop(LoggerContext context) {
	}

	@Override
	public void onLevelChange(Logger logger, Level level) {
		if(!initialized) {
			MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
			mc.addMailcap("text/html;; x-java-content-handler=org.apache.geronimo.mail.handlers.HtmlHandler");
			mc.addMailcap("text/xml;; x-java-content-handler=org.apache.geronimo.mail.handlers.XMLHandler");
			mc.addMailcap("text/plain;; x-java-content-handler=org.apache.geronimo.mail.handlers.TextHandler");
			mc.addMailcap(
					"multipart/*;; x-java-content-handler=org.apache.geronimo.mail.handlers.MultipartHandler; x-java-fallback-entry=true");
			mc.addMailcap(
					"multipart/mixed;; x-java-content-handler=org.apache.geronimo.mail.handlers.MultipartHandler; x-java-fallback-entry=true");
			mc.addMailcap("message/rfc822;; x-java-content-handler=org.apache.geronimo.mail.handlers.MessageHandler");
			CommandMap.setDefaultCommandMap(mc);
			initialized = true;
		}
	}

}
