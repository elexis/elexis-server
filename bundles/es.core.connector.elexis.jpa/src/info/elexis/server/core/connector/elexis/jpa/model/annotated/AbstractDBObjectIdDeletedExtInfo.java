package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.util.Hashtable;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Convert;

@MappedSuperclass
public abstract class AbstractDBObjectIdDeletedExtInfo extends AbstractDBObjectIdDeleted {

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisExtInfoMapConverter")
	protected Map<Object, Object> extInfo = new Hashtable<Object, Object>();

	public Map<Object, Object> getExtInfo() {
		return extInfo;
	}

	public void setExtInfo(Map<Object, Object> extInfo) {
		this.extInfo = extInfo;
	}

	@Transient
	public String getExtInfoAsString(Object key) {
		return (String) getExtInfo().get(key);
	}

	@Transient
	public String getLabel() {
		return getId() + "@" + getClass().getName() + " " + isDeleted();
	};
}
