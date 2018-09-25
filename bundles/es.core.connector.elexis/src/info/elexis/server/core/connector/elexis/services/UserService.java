package info.elexis.server.core.connector.elexis.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.rgw.tools.PasswordEncryptionService;
import info.elexis.server.core.connector.elexis.services.internal.CoreModelServiceHolder;

public class UserService extends PersistenceService2 {
	
	private static Logger log = LoggerFactory.getLogger(UserService.class);
	
	//	public static class Builder extends AbstractBuilder<User> {
	//		public Builder(String username, Kontakt mandant){
	//			object = new User();
	//			object.setId(username);
	//			object.setKontakt(mandant);
	//			object.setActive(true);
	//			object.setSalt("invalid");
	//			object.setHashedPassword("invalid");
	//			
	//			Optional<Role> role = RoleService.load(RoleConstants.SYSTEMROLE_LITERAL_USER);
	//			object.getRoles().add(role.get());
	//		}
	//	}
	
	/**
	 * Determine whether a given user has a role
	 * 
	 * @param u
	 * @param role
	 * @return
	 */
	public static boolean userHasRole(IUser u, String role){
		if (u == null || role == null) {
			throw new IllegalArgumentException();
		}
		Collection<IRole> roles = u.getRoles();
		long count = roles.stream().filter(f -> role.equalsIgnoreCase(f.getId())).count();
		return (count > 0l);
	}
	
	/**
	 * Find the user associated with a given contact, if available
	 * 
	 * @param contact
	 * @return
	 */
	public static Optional<IUser> findByContact(IContact contact){
		if (contact == null) {
			return Optional.empty();
		}
		IQuery<IUser> qre = CoreModelServiceHolder.get().getQuery(IUser.class);
		qre.and(ModelPackage.Literals.IUSER__ASSIGNED_CONTACT, COMPARATOR.EQUALS, contact);
		List<IUser> result = qre.execute();
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
	public static boolean verifyPassword(IUser user, String attemptedPassword){
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
	public static void setPasswordForUser(IUser user, String password){
		if (user != null) {
			PasswordEncryptionService pes = new PasswordEncryptionService();
			try {
				String salt = pes.generateSaltAsHexString();
				String hashed_pw = pes.getEncryptedPasswordAsHexString(password, salt);
				user.setSalt(salt);
				user.setHashedPassword(hashed_pw);
				CoreModelServiceHolder.get().save(user);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
				log.warn("Error verifying password for user [{}].", user.getLabel(), e);
			}
		}
		
	}
	
	public static Optional<IContact> findKontaktByUserId(String userId){
		if (StringUtils.isNotEmpty(userId)) {
			Optional<IUser> user = CoreModelServiceHolder.get().load(userId, IUser.class);
			if (user.isPresent()) {
				return Optional.ofNullable(user.get().getAssignedContact());
			}
		}
		return Optional.empty();
	}
	
}
