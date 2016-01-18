package info.elexis.server.core.util;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CoreUtil {
	public static Path getHomeDirectory() {
		String userHomeProp = System.getProperty("user.home");
		File homedir = new File(new File(userHomeProp), "elexis-server");
		if (!homedir.exists()) {
			homedir.mkdir();
		}
		return Paths.get(homedir.toURI());
	}
}
