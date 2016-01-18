package info.elexis.server.core.security;

import java.net.URL;
import java.nio.file.Path;

import org.apache.shiro.realm.text.IniRealm;

import info.elexis.server.core.internal.Activator;
import info.elexis.server.core.util.CoreUtil;

/**
 * This authorization realm is always included. If no ${user.home}/shiro.ini is
 * provided, it reads the internal default file, allowing for access of admin:admin.
 * 
 * It is heavily required to override the given default, as otherwise admin access
 * is easily provided to everyone.
 *
 */
public class DefaultAuthorizingRealm extends IniRealm {
	
	private static final String iniLocation;
	
	static {
		Path path = CoreUtil.getHomeDirectory().resolve("shiro.ini");
		if(path.toFile().exists()) {
			iniLocation = path.toString();
		} else {
			URL resource = Activator.loadResourceFile("shiro.ini");
			iniLocation = "url:"+resource.toString();
		}
	}
	
	public DefaultAuthorizingRealm() {
		super(iniLocation);
	}
}
