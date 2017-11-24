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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractAuthenticationFile {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private final Path filePath;
	private final char delimiter;
	private final String description;

	private Map<String, ESAFLine> entries = Collections.synchronizedMap(new HashMap<>());

	public AbstractAuthenticationFile(Path filePath, char delimiter, String description) {
		this.filePath = filePath;
		this.delimiter = delimiter;
		this.description = description;
	}

	public Map<String, ESAFLine> getEntries() {
		return entries;
	}

	public void loadFile() {
		try {
			if (filePath.toFile().exists() && filePath.toFile().canRead()) {
				List<String> lines = Files.readAllLines(filePath);
				for (String line : lines) {
					if (line.startsWith("#")) {
						continue;
					}
					String[] lineTokens = line.split(Character.toString(delimiter));
					if (lineTokens.length == 4) {
						ESAFLine el = new ESAFLine(lineTokens);
						entries.put(el.getId(), el);
					}
				}
			} else {
				log.warn("Can not read [{}].", filePath.toFile().getAbsolutePath());
			}
		} catch (IOException ioe) {
			log.error("Error reading [{}]", filePath.toFile().getAbsolutePath(), ioe);
			entries.clear();
		}
	}

	public void saveFile() throws IOException {
		List<String> lines = new ArrayList<>();
		lines.add("# " + description + "; file written " + LocalDateTime.now());
		entries.values().stream().map(e -> e.toString()).forEach(e -> lines.add(e));
		Files.write(filePath, lines);
	}

	public boolean isInitialized() {
		return entries.size() > 0;
	}

	/**
	 * 
	 * @param clientId
	 * @return <code>null</code> if no entry present
	 */
	public String getHashedPasswordForClient(String clientId) {
		if (entries.containsKey(clientId)) {
			return entries.get(clientId).getHashedPassword();
		}
		return null;
	}

	public Set<String> getRolesForClientId(String clientId) {
		if (entries.containsKey(clientId)) {
			return entries.get(clientId).getRoles();
		}
		return Collections.emptySet();
	}

	public String addOrReplaceId(String clientId, Set<String> roles, String password) throws IOException {
		if (password == null) {
			password = UUID.randomUUID().toString().replaceAll("-", "");
		}

		if (roles == null) {
			roles = Collections.emptySet();
		}

		DefaultPasswordService defaultPasswordService = new DefaultPasswordService();
		String encryptedPassword = defaultPasswordService.encryptPassword(password);

		ESAFLine userLine = new ESAFLine(new String[] { clientId, String.join(",", roles), encryptedPassword });
		entries.put(clientId, userLine);
		saveFile();
		return password;
	}

	/**
	 * Clear all entries in the hashmap and remove the password file
	 * 
	 * @throws IOException
	 */
	public void clearAndRemove() throws IOException {
		entries.clear();
		Files.deleteIfExists(filePath);
	}

	protected class ESAFLine {

		private String id;
		private Set<String> roles;
		private String hashedPassword;

		public ESAFLine(String[] lineTokens) {
			id = lineTokens[0];
			roles = Arrays.asList(lineTokens[1].split(",")).stream().collect(Collectors.toSet());
			hashedPassword = lineTokens[2];
		}

		public String getId() {
			return id;
		}

		public Set<String> getRoles() {
			return roles;
		}

		public String getHashedPassword() {
			return hashedPassword;
		}

		@Override
		public String toString() {
			return id + delimiter + String.join(",", roles) + delimiter + hashedPassword;
		}
	}

}
