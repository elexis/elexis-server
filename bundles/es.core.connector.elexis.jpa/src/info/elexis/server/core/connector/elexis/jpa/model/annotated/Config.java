package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Cache;
import org.eclipse.persistence.annotations.CacheType;

@Entity
@Table(name = "config")
@Cache(type=CacheType.NONE)
public class Config extends AbstractDBObject implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(unique = true, nullable = false, length = 80)
	private String param;

	private BigInteger lastupdate;

	@Lob
	private String wert;

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}

	public BigInteger getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(BigInteger lastupdate) {
		this.lastupdate = lastupdate;
	}

	public String getWert() {
		return wert;
	}

	public void setWert(String wert) {
		this.wert = wert;
	}
}
