package info.elexis.server.core.connector.elexis.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import info.elexis.server.core.connector.elexis.services.internal.CoreModelServiceHolder;

public class UserService extends PersistenceService2 {
	
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
