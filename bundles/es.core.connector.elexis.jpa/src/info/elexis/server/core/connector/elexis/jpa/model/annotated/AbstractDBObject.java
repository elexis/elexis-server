package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.math.BigInteger;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.listener.AbstractDBObjectEntityListener;

@MappedSuperclass
@EntityListeners(AbstractDBObjectEntityListener.class)
public abstract class AbstractDBObject {
	
	// Transparently updated by the EntityListener
	protected BigInteger lastupdate;
	
	public BigInteger getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(BigInteger lastupdate) {
		this.lastupdate = lastupdate;
	}
	
	public String getLabel(){
		return getClass().getSimpleName();
	};
}
