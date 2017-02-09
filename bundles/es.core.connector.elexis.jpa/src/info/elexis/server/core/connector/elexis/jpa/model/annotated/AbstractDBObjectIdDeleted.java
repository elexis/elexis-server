package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.util.Map;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import org.eclipse.persistence.annotations.Convert;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.id.ElexisIdGenerator;

@MappedSuperclass
public abstract class AbstractDBObjectIdDeleted extends AbstractDBObject {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id = ElexisIdGenerator.generateId();

	@Column
	@Convert("booleanStringConverter")
	protected boolean deleted = false;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "object", insertable = false, updatable = false)
	@MapKey(name = "domain")
	protected Map<String, Xid> xids;

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

	public Map<String, Xid> getXids() {
		return xids;
	}

	public void setXids(Map<String, Xid> xids) {
		this.xids = xids;
	}

	public String getLabel() {
		return super.getLabel() + (isDeleted() ? " D " : "   ") + " [" + String.format("%25S", getId()) + "]";
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
		return this.getClass().getSimpleName() + " [id=" + id + ", deleted=" + deleted + "]";
	}
}
