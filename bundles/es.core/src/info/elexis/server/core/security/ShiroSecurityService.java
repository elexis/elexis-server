package info.elexis.server.core.security;

import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionException;
import org.apache.shiro.session.mgt.DefaultSessionKey;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.internal.Activator;
import info.elexis.server.core.internal.Application;

public class ShiroSecurityService {

	public static final String SESSION_USERID = "session.user.id";
	public static final String USER_IS_ADMIN = "user.admin";

	private static Logger log = LoggerFactory.getLogger(ShiroSecurityService.class);

	private static SecurityManager shiroSecurityManager = ShiroAuthorizingRealmsManager.getSecurityManager();

	static {
//		Path path = Application.getHomeDirectory().resolve("shiro.ini");
//		Realm realm;
//		if (path.toFile().exists()) {
//			realm = new IniRealm(path.toString());
//			log.info("Initialized security realm from " + path.toString());
//		} else {
//			URL resource = Activator.loadResourceFile("shiro.ini");
//			realm = new IniRealm("url:" + resource.toString());
//			log.warn("Using fallback security realm. Please provide a configuration in "
//					+ Application.getHomeDirectory().resolve("shiro.ini").toString());
//		}

		// TODO add ElexisConnectorSecurityManager

	}

	public static Optional<Serializable> authenticate(String userID, char[] password) {
		log.info("Authenticating " + userID + " @ " + shiroSecurityManager);

		UsernamePasswordToken token = new UsernamePasswordToken(userID, password);
		Subject subject = new Subject.Builder().buildSubject();
		try {
			subject.login(token);

			Session session = subject.getSession();
			session.setAttribute(SESSION_USERID, userID);
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
