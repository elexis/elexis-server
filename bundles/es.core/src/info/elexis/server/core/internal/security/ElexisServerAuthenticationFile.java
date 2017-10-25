package info.elexis.server.core.internal.security;

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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
					if (lineTokens.length == 4) {
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

	public static void saveFile() throws IOException {
		List<String> lines = new ArrayList<>();
		lines.add("# Elexis-Server Password file written " + LocalDateTime.now());
		entries.values().stream().map(e -> e.toString()).forEach(e -> lines.add(e));
		Files.write(PASSWD_PATH, lines);
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
		private String apiKey;

		public ESAFLine(String[] lineTokens) {
			username = lineTokens[0];
			roles = Arrays.asList(lineTokens[1].split(",")).stream().collect(Collectors.toSet());
			hashedPassword = lineTokens[2];
			apiKey = lineTokens[3];
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

		public String getApiKey() {
			return apiKey;
		}

		@Override
		public String toString() {
			return username + ":" + StringUtils.join(roles.iterator(), ",") + ":" + hashedPassword + ":" + apiKey;
		}
	}

	public static boolean isInitialized() {
		return entries.size() > 0;
	}

	/**
	 * Sets the initial password of the {@link ElexisServerWebConstants#ES_ADMIN}
	 * user. Can be set only once. In order to be able to call it again, the
	 * password file needs to be deleted.
	 * 
	 * @param password
	 * @throws IOException
	 * @throws SecurityException
	 *             if the password was already set
	 * @return an apiKey generated for the user
	 */
	public synchronized static String setInitialEsAdminPassword(String password) throws IOException {
		if (entries.containsKey(SecurityConstants.ES_ADMIN)) {
			throw new SecurityException(
					SecurityConstants.ES_ADMIN + " user is already instantiated, please delete file or entry line");
		}

		DefaultPasswordService defaultPasswordService = new DefaultPasswordService();
		String encryptedPassword = defaultPasswordService.encryptPassword(password);
		String apiKey = UUID.randomUUID().toString().replaceAll("-", "");

		ESAFLine esadminLine = new ESAFLine(
				new String[] { SecurityConstants.ES_ADMIN, SecurityConstants.ES_ADMIN, encryptedPassword, apiKey });
		entries.put(SecurityConstants.ES_ADMIN, esadminLine);
		saveFile();
		return esadminLine.getApiKey();
	}

	/**
	 * Clear all entries in the hashmap and remove the password file
	 * 
	 * @throws IOException
	 */
	public static void clearAndRemove() throws IOException {
		entries.clear();
		Files.deleteIfExists(PASSWD_PATH);
	}

	/**
	 * Finds the user providing its apiKey
	 * 
	 * @param apiKey
	 * @return the userid if found, else <code>null</code>
	 */
	public static String getUserByApiKey(String apiKey) {
		Optional<ESAFLine> user = entries.values().stream().filter(v -> v.getApiKey().equals(apiKey)).findFirst();
		if (user.isPresent()) {
			return user.get().getUsername();
		}
		return null;
	}
}
