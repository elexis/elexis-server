package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

import org.eclipse.persistence.annotations.Convert;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.listener.AbstractDBObjectEntityListener;

@MappedSuperclass
@EntityListeners(AbstractDBObjectEntityListener.class)
public abstract class AbstractDBObjectIdDeleted extends AbstractDBObject {

	@Id
	@GeneratedValue(generator = "system-uuid")
	@Column(unique = true, nullable = false, length = 25)
	private String id;

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
	};
}
