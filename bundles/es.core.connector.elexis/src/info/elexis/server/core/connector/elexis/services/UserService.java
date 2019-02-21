package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import info.elexis.server.core.connector.elexis.services.internal.CoreModelServiceHolder;

public class UserService extends PersistenceService2 {

	/**
	 * Find the user associated with a given contact, if available
	 * 
	 * @param contact
	 * @return
	 */
	public static Optional<IUser> findByContact(IContact contact) {
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

}
