package info.elexis.server.core.security;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.pam.FirstSuccessfulStrategy;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ShiroAuthorizingRealmsManager {

	private static Logger log = LoggerFactory.getLogger(ShiroAuthorizingRealmsManager.class);
	
	private static DefaultSecurityManager shiroSecurityManager;
	private static Collection<Realm> allRealms = new LinkedList<Realm>();
	
	static {
		allRealms.add(new DefaultAuthorizingRealm());
		updateSecurityManager();
	}
	
	@Reference(service = Realm.class, 
			cardinality = ReferenceCardinality.AT_LEAST_ONE, 
			policy = ReferencePolicy.STATIC, 
			unbind = "removeShiroRealm")
	protected void addShiroRealm(Realm sar) {
		if(ShiroAuthorizingRealmsManager.allRealms.contains(sar)) {
			log.warn("Tried to add already included realm "+sar);
			return;
		}
		
		log.info("Adding security realm {}", sar);
		ShiroAuthorizingRealmsManager.allRealms.add(sar);
		ShiroAuthorizingRealmsManager.updateSecurityManager();
	}
	
	protected void removeShiroRealm(Realm sar) {
		if(ShiroAuthorizingRealmsManager.allRealms.contains(sar)) {
			ShiroAuthorizingRealmsManager.allRealms.remove(sar);
			ShiroAuthorizingRealmsManager.updateSecurityManager();
		}
		log.info("Removing security realm {}", sar);
	}
	
	private static synchronized void updateSecurityManager() {
		Collection<Realm> allRealms = ShiroAuthorizingRealmsManager.getAllRealms();
		
		shiroSecurityManager = new DefaultSecurityManager(allRealms);
		
		
		ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
		FirstSuccessfulStrategy firstSuccessfulStrategy = new FirstSuccessfulStrategy();
		authenticator.setAuthenticationStrategy(firstSuccessfulStrategy);
		shiroSecurityManager.setAuthenticator(authenticator);

		SecurityUtils.setSecurityManager(shiroSecurityManager);
	}
	
	public static Collection<Realm> getAllRealms() {
		return allRealms;
	}

	public static SecurityManager getSecurityManager() {
		return shiroSecurityManager;
	}
	
}
