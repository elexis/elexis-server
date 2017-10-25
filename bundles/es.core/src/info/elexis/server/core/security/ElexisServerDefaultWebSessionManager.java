package info.elexis.server.core.security;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionManager;

public class ElexisServerDefaultWebSessionManager implements WebSessionManager {

	private static DefaultWebSessionManager dwsm = new DefaultWebSessionManager();
	
	public ElexisServerDefaultWebSessionManager() {
		// we have to hand out cookies that are valid for the entire server
		dwsm.getSessionIdCookie().setPath("/");
	}
	
	@Override
	public Session start(SessionContext context) {
		return dwsm.start(context);
	}

	@Override
	public Session getSession(SessionKey key) throws SessionException {
		return dwsm.getSession(key);
	}

	@Override
	public boolean isServletContainerSessions() {
		return dwsm.isServletContainerSessions();
	}

}