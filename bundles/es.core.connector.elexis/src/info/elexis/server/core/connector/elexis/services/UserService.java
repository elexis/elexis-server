package info.elexis.server.core.connector.elexis.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.RoleConstants;
import ch.rgw.tools.PasswordEncryptionService;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class UserService extends PersistenceService {

	private static Logger log = LoggerFactory.getLogger(UserService.class);

	public static class Builder extends AbstractBuilder<User> {
		public Builder(String username, Kontakt mandant) {
			object = new User();
			object.setId(username);
			object.setKontakt(mandant);
			object.setActive(true);
			object.setSalt("invalid");
			object.setHashedPassword("invalid");

			Optional<Role> role = RoleService.load(RoleConstants.SYSTEMROLE_LITERAL_USER);
			object.getRoles().add(role.get());
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<User> load(String id) {
		return PersistenceService.load(User.class, id).map(v -> (User) v);
	}

	/**
	 * convenience method
	 * 
	 * @param includeElementsMarkedDeleted
	 * @return
	 */
	public static List<User> findAll(boolean includeElementsMarkedDeleted) {
		return PersistenceService.findAll(User.class, includeElementsMarkedDeleted).stream().map(v -> (User) v)
				.collect(Collectors.toList());
	}

	/**
	 * Determine whether a given user has a role
	 * 
	 * @param u
	 * @param role
	 * @return
	 */
	public static boolean userHasRole(User u, String role) {
		if (u == null || role == null) {
			throw new IllegalArgumentException();
		}
		Collection<Role> roles = u.getRoles();
		long count = roles.stream().filter(f -> role.equalsIgnoreCase(f.getId())).count();
		return (count > 0l);
	}

	/**
	 * Find the user associated with a given contact, if available
	 * 
	 * @param kontakt
	 * @return
	 */
	public static Optional<User> findByKontakt(Kontakt kontakt) {
		if (kontakt == null) {
			return Optional.empty();
		}
		JPAQuery<User> qre = new JPAQuery<User>(User.class);
		qre.add(User_.kontakt, JPAQuery.QUERY.EQUALS, kontakt);
		List<User> result = qre.execute();
		if (result.size() == 1) {
			return Optional.of(result.get(0));
		} else {
			return Optional.empty();
		}
	}

	/**
	 * Verify the provided password for the given user
	 * 
	 * @param user
	 * @param attemptedPassword
	 * @return <code>true</code> if password matched
	 */
	public static boolean verifyPassword(User user, String attemptedPassword) {
		boolean ret = false;

		if (user != null) {
			PasswordEncryptionService pes = new PasswordEncryptionService();
			try {
				ret = pes.authenticate(attemptedPassword, user.getHashedPassword(), user.getSalt());
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
				log.warn("Error verifying password for user [{}].", user.getLabel(), e);
			}
		}

		return ret;
	}

	/**
	 * Set the password for a given user
	 * 
	 * @param user
	 * @param password
	 */
	public static void setPasswordForUser(User user, String password) {
		if (user != null) {
			PasswordEncryptionService pes = new PasswordEncryptionService();
			try {
				String salt = pes.generateSaltAsHexString();
				String hashed_pw = pes.getEncryptedPasswordAsHexString(password, salt);
				user.setSalt(salt);
				user.setHashedPassword(hashed_pw);
				save(user);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
				log.warn("Error verifying password for user [{}].", user.getLabel(), e);
			}
		}

	}
	
	/**
	 * Find a user by its apiKey
	 * 
	 * @param apiKey
	 * @param includeInactive
	 * @return
	 */
	public static Optional<User> findByApiKey(String apiKey, boolean includeInactive) {
		JPAQuery<User> query = new JPAQuery<User>(User.class);
		query.add(User_.apiKey, QUERY.EQUALS, apiKey);
		if (!includeInactive) {
			query.add(User_.active, QUERY.EQUALS, true);
		}
		return query.executeGetSingleResult();
	}
}
