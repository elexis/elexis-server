package info.elexis.server.core.security.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.common.util.CoreUtil;
import info.elexis.server.core.constants.SecurityConstants;

public class ElexisServerAuthenticationFile {

	private static Logger log = LoggerFactory.getLogger(ElexisServerAuthenticationFile.class);

	public static final String FILENAME = "elexis-server.auth";
	public static final Path PASSWD_PATH = CoreUtil.getHomeDirectory().resolve(FILENAME);

	private static Map<String, ESAFLine> entries = new HashMap<>();

	public static void loadFile() {
		try {
			if (PASSWD_PATH.toFile().exists() && PASSWD_PATH.toFile().canRead()) {
				List<String> lines = Files.readAllLines(PASSWD_PATH);
				for (String line : lines) {
					if (line.startsWith("#")) {
						continue;
					}
					String[] lineTokens = line.split(":");
					if (lineTokens.length == 3) {
						ESAFLine el = new ESAFLine(lineTokens);
						entries.put(el.getUsername(), el);
					}
				}
			} else {
				log.warn("Can not read [{}].", PASSWD_PATH.toFile().getAbsolutePath());
			}
		} catch (IOException ioe) {
			log.error("Error reading [{}]", PASSWD_PATH.toFile().getAbsolutePath(), ioe);
			entries.clear();
		}
	}

	public static void saveFile() {
		List<String> lines = new ArrayList<>();
		lines.add("# Elexis-Server Password file written " + LocalDateTime.now());
		entries.values().stream().map(e -> e.toString()).forEach(e -> lines.add(e));
		try {
			Files.write(PASSWD_PATH, lines);
		} catch (IOException e1) {
			log.warn("Error writing password file [{}]", PASSWD_PATH.toFile().getAbsolutePath(), e1);
		}
	}

	/**
	 * 
	 * @param userid
	 * @return <code>null</code> if no entry present
	 */
	public static String getHashedPasswordForUserId(String userid) {
		if (entries.containsKey(userid)) {
			return entries.get(userid).getHashedPassword();
		}
		return null;
	}

	public static Set<String> getRolesForUserId(String userid) {
		if (entries.containsKey(userid)) {
			return entries.get(userid).getRoles();
		}
		return Collections.emptySet();
	}

	private static class ESAFLine {

		private String username;
		private Set<String> roles;
		private String hashedPassword;

		public ESAFLine(String[] lineTokens) {
			username = lineTokens[0];
			roles = Arrays.asList(lineTokens[1].split(",")).stream().collect(Collectors.toSet());
			hashedPassword = lineTokens[2];
		}

		public String getUsername() {
			return username;
		}

		public Set<String> getRoles() {
			return roles;
		}

		public String getHashedPassword() {
			return hashedPassword;
		}

		@Override
		public String toString() {
			return username + ":" + StringUtils.join(roles.iterator(), ",") + ":" + hashedPassword;
		}
	}

	public static boolean isInitialized() {
		return entries.size() > 0;
	}

	/**
	 * Sets the initial password of the {@link ElexisServerWebConstants#ES_ADMIN} user. Can be set only
	 * once. In order to be able to call it again, the password file needs to be
	 * deleted.
	 * 
	 * @param password
	 * @throws SecurityException
	 *             if the password was already set
	 */
	public static void setInitialEsAdminPassword(String password) {
		if (entries.containsKey(SecurityConstants.ES_ADMIN)) {
			throw new SecurityException(SecurityConstants.ES_ADMIN + " user is already instantiated, please delete file or entry line");
		}

		DefaultPasswordService defaultPasswordService = new DefaultPasswordService();
		String encryptedPassword = defaultPasswordService.encryptPassword(password);

		ESAFLine esadminLine;
		if (entries.containsKey(SecurityConstants.ES_ADMIN)) {
			esadminLine = entries.get(SecurityConstants.ES_ADMIN);
		} else {
			esadminLine = new ESAFLine(new String[] { SecurityConstants.ES_ADMIN, SecurityConstants.ES_ADMIN, encryptedPassword });
		}
		entries.put(SecurityConstants.ES_ADMIN, esadminLine);
		saveFile();
	}
}
