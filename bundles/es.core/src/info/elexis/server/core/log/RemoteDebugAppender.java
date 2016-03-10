package info.elexis.server.core.log;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

public class RemoteDebugAppender extends AppenderBase<ILoggingEvent> {

	private boolean remoteDebugIsEnabled;

	@Override
	protected void append(ILoggingEvent ev) {
		if(!remoteDebugIsEnabled) {
			return;
		}
		
		// lets connect to remote
	
		
		System.out.println("RemoteDebugAppender: "+ev);

	}

}
