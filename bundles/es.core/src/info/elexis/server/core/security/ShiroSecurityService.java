package info.elexis.server.core.security;

import java.io.Serializable;
import java.util.Optional;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiroSecurityService {

	public static final String DEFAULT_REALM = "elexis-server";

	private static Logger log = LoggerFactory.getLogger(ShiroSecurityService.class);

	private static SecurityManager shiroSecurityManager = ShiroAuthorizingRealmsManager.getSecurityManager();
	
	public static Optional<Serializable> authenticate(String userID, char[] password) {
		log.info("Authenticating " + userID + " @ " + shiroSecurityManager);

		UsernamePasswordToken token = new UsernamePasswordToken(userID, password);
		Subject subject = new Subject.Builder().buildSubject();
		try {
			subject.login(token);
			
			Session session = subject.getSession();
			log.info("Created session " + session.getId() + " for user " + userID + " @ " + shiroSecurityManager);
			return Optional.of(session.getId());
		} catch (AuthenticationException uae) {
			log.warn("Invalid authentication request for user {}: {}", userID, uae.getLocalizedMessage());
			uae.printStackTrace();
		}

		return Optional.empty();
	}

	public static Object getSessionAttribute(Serializable sessionId, Object key) {
		SessionKey sessionKey = new DefaultSessionKey(sessionId);
		Session session = shiroSecurityManager.getSession(sessionKey);
		if (session != null) {
			return session.getAttribute(key);
		}
		return null;
	}

	public static void setSessionAttribute(Serializable sessionId, Object key, Object value) {
		log.info("Get Session " + sessionId + " @ " + shiroSecurityManager);
		SessionKey sessionKey = new DefaultSessionKey(sessionId);
		Session session = shiroSecurityManager.getSession(sessionKey);
		if (session != null) {
			session.setAttribute(key, value);
		}
	}

	public static boolean hasPermission(Serializable sessionId, String string) {
		SessionKey sessionKey = new DefaultSessionKey(sessionId);
		Session session = shiroSecurityManager.getSession(sessionKey);

		Subject subject = new Subject.Builder().session(session).buildSubject();
		WildcardPermission permission = new WildcardPermission(string);
		return subject.isPermitted(permission);
	}

	public static Optional<Subject> getSubjectBySessionId(Serializable sessionId) {
		SessionKey sessionKey = new DefaultSessionKey(sessionId);
		try {
			Session session = shiroSecurityManager.getSession(sessionKey);
			return Optional.of(new Subject.Builder().session(session).buildSubject());
		} catch (SessionException se) {
			log.warn("Tried to get subject for non existing session " + sessionId, se);
		}
		return Optional.ofNullable(null);
	}

}
