package info.elexis.server.core.connector.elexis.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User_;

public class UserService extends AbstractService<User> {

	public static UserService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final UserService INSTANCE = new UserService();
	}

	private UserService() {
		super(User.class);
	}

	public boolean userHasRole(User u, String role) {
		if (u == null || role == null) {
			throw new IllegalArgumentException();
		}
		Collection<Role> roles = u.getRoles();
		long count = roles.stream().filter(f -> role.equalsIgnoreCase(f.getId())).count();
		return (count > 0l);
	}

	public Optional<User> findByKontakt(Kontakt kontakt) {
		if(kontakt==null) {
			return Optional.empty();
		}
		JPAQuery<User> qre = new JPAQuery<User>(User.class);
		qre.add(User_.kontakt, JPAQuery.QUERY.EQUALS, kontakt);
		List<User> result = qre.execute();
		if(result.size()==1) {
			return Optional.of(result.get(0));
		} else {
			return Optional.empty();
		}
		
	}
}
