package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "ROLE")
public class Role extends AbstractDBObjectWithExtInfo {

	@Convert("booleanStringConverter")
	@Column(name = "ISSYSTEMROLE")
	protected boolean systemRole;

	public boolean isSystemRole() {
		return systemRole;
	}

	public void setSystemRole(boolean systemRole) {
		this.systemRole = systemRole;
	}
}
