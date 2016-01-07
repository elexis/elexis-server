package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.FetchType;
import javax.persistence.MappedSuperclass;

import org.eclipse.persistence.annotations.Convert;

@MappedSuperclass
public abstract class AbstractDBObjectWithExtInfo extends AbstractDBObject {

	@Basic(fetch = FetchType.LAZY)
	@Convert(value = "ElexisExtInfoMapConverter")
	protected Map<Object, Object> extInfo;
	
	public Map<Object, Object> getExtInfo() {
		return extInfo;
	}

	public void setExtInfo(Map<Object, Object> extInfo) {
		this.extInfo = extInfo;
	}
}
