package info.elexis.server.core.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.common.security.ESAuthorizingRealm;

/**
 * Composite authentication realm. Allows for registration of additional realms.
 */
@Component(service = {})
public class ElexisServerCompositeRealm extends AuthorizingRealm {

	private static Logger log = LoggerFactory.getLogger(ElexisServerCompositeRealm.class);

	private static Map<String, ESAuthorizingRealm> realms = Collections.synchronizedMap(new HashMap<>());

	@Reference(service = ESAuthorizingRealm.class, cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	protected synchronized void bind(ESAuthorizingRealm realm) {
		log.info("Binding realm [{}] as [{}]", realm.getClass().getName(), realm.getName());
		realms.put(realm.getName(), realm);
	}

	protected synchronized void unbind(ESAuthorizingRealm realm) {
		log.info("Unbinding realm [{}]", realm.getClass().getName());
		realms.remove(realm.getName());
	}

	public ElexisServerCompositeRealm() {
		super(credentialsMatcher);
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		if (principals == null) {
			throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
		}

		Set<String> realmNames = principals.getRealmNames();
		if (realmNames.size() == 1) {
			ESAuthorizingRealm ar = realms.get(realmNames.iterator().next());
			if (ar != null) {
				AuthorizationInfo authInfo = ar.doGetAuthorizationInfo(principals);
				return authInfo;
			}
		} else {
			log.warn("AuthenticationInfo does not match against single realm [{}]", realmNames);
		}

		return null;
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		try {
			for (ESAuthorizingRealm realm : realms.values()) {
				if (realm.supports(token)) {
					AuthenticationInfo authenticationInfo = realm.doGetAuthenticationInfo(token);
					if (authenticationInfo != null) {
						return authenticationInfo;
					}
				}
			}
		} catch (AuthenticationException ae) {
			log.warn("AuthenticationException", ae);
			throw (ae);
		}

		log.warn("Invalid login attempt for userId [{}] no realm entry found.", token.getPrincipal());

		return null;
	}

	/**
	 * Forward the credentials match request to the resp. realm responsible for it
	 */
	private static final CredentialsMatcher credentialsMatcher = new CredentialsMatcher() {

		@Override
		public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
			Set<String> realmNames = info.getPrincipals().getRealmNames();
			if (realmNames.size() == 1) {
				ESAuthorizingRealm ar = realms.get(realmNames.iterator().next());
				if (ar != null) {
					boolean result = ar.getCredentialsMatcher().doCredentialsMatch(token, info);
					if (!result) {
						log.warn("Invalid login attempt by userId or token [{}] in realm [{}]", token.getPrincipal(),
								ar.getName());
					}
					return result;
				}
			} else {
				log.warn("AuthenticationInfo does not match against single realm [{}]", realmNames);
			}

			return false;
		}
	};

	@Override
	public boolean supports(AuthenticationToken token) {
		return true;
	}
}
