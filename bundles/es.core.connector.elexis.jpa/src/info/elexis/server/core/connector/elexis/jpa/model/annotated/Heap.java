package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.converter.ElexisDBStringDateConverter;

@Entity
@Table(name = "HEAP")
public class Heap extends AbstractDBObjectIdDeleted {

	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	protected Date datum;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob()
	protected byte[] inhalt;
	
	public Date getDatum() {
		return datum;
	}
	
	public void setDatum(Date datum) {
		this.datum = datum;
	}
	
	public byte[] getInhalt() {
		return inhalt;
	}
	
	public void setInhalt(byte[] inhalt) {
		this.inhalt = inhalt;
	}
}
