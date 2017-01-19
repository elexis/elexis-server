package info.elexis.server.core.connector.elexis.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User_;

public class UserService extends PersistenceService {

	public static class Builder extends AbstractBuilder<User> {
		public Builder(String username, Kontakt mandant) {
			object = new User();
			object.setId(username);
			object.setKontakt(mandant);
			object.setActive(true);
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

	public static boolean userHasRole(User u, String role) {
		if (u == null || role == null) {
			throw new IllegalArgumentException();
		}
		Collection<Role> roles = u.getRoles();
		long count = roles.stream().filter(f -> role.equalsIgnoreCase(f.getId())).count();
		return (count > 0l);
	}

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
}
