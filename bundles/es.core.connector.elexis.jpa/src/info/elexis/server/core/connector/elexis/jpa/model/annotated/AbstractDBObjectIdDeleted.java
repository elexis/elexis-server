package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

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
	
	public String getLabel(){
		return getId()+"@"+getClass().getName()+" "+isDeleted();
	};
}
