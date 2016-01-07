package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;

@Entity
@Table(name = "HEAP")
public class Heap extends AbstractDBObject {

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
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
