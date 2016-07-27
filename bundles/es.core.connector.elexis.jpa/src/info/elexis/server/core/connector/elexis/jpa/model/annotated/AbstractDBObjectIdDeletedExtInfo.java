package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.util.Hashtable;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.eclipse.persistence.annotations.Convert;

@MappedSuperclass
public abstract class AbstractDBObjectIdDeletedExtInfo extends AbstractDBObjectIdDeleted {

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisExtInfoMapConverter")
	@Column(columnDefinition = "BLOB")
	protected Map<Object, Object> extInfo = new Hashtable<Object, Object>();

	protected Map<Object, Object> getExtInfo() {
		return extInfo;
	}

	private void setExtInfo(Map<Object, Object> extInfo) {
		this.extInfo = extInfo;
	}

	@Transient
	public void setExtInfoValue(Object key, Object value) {
		// we have to create a new object on change
		// otherwise JPA won't pick-up the change
		Hashtable<Object, Object> ht = new Hashtable<Object, Object>(getExtInfo());
		ht.put(key, value);
		setExtInfo(ht);
	}
	
	@Transient
	public String getExtInfoAsString(Object key) {
		return (String) getExtInfo().get(key);
	}

	@Transient
	public String getLabel() {
		return getId() + "@" + getClass().getName() + " [isDeleted " + isDeleted()+"]";
	};
}
