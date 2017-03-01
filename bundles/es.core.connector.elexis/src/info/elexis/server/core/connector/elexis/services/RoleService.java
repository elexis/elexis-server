package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Role;

public class RoleService extends PersistenceService {

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Role> load(String id) {
		return PersistenceService.load(Role.class, id).map(v -> (Role) v);
	}
	
}
