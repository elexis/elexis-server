package info.elexis.server.findings.fhir.jpa.model.annotated;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.eclipse.persistence.annotations.Convert;

import ch.elexis.core.model.Identifiable;
import info.elexis.server.findings.fhir.jpa.model.annotated.id.ElexisIdGenerator;

@MappedSuperclass
public abstract class AbstractDBObjectIdDeleted extends AbstractDBObject implements Identifiable{

	@Id
	@GeneratedValue(generator = "findings-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert("booleanStringConverter")
	protected boolean deleted = false;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
		// TODO if true, remove all Xids
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractDBObjectIdDeleted other = (AbstractDBObjectIdDeleted) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getClass(), id);
	}

	@Override
	public String toString() {
		return super.toString() + (isDeleted() ? " D " : "   ") + "id=[" + String.format("%25S", getId()) + "]";
	}
}
