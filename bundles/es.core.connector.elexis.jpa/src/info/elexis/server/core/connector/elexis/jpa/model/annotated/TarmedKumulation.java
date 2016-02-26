package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;

@Entity
@Table(name = "TARMED_KUMULATION")
public class TarmedKumulation extends AbstractDBObjectIdDeleted {

	public static final String TYP_EXCLUSION = "E";
	public static final String TYP_INCLUSION = "I";
	public static final String TYP_EXCLUSIVE = "X";
	
	@Column(length = 25)
	private String masterCode;

	@Column(length = 1)
	private String masterArt;

	@Column(length = 25)
	private String slaveCode;

	@Column(length = 1)
	private String slaveArt;

	@Column(length = 1)
	private String typ;

	@Column(length = 1)
	private String view;

	@Column(length = 1)
	private String validSide;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	private LocalDate validFrom;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	private LocalDate validTo;

	public String getMasterCode() {
		return masterCode;
	}

	public void setMasterCode(String masterCode) {
		this.masterCode = masterCode;
	}

	public String getMasterArt() {
		return masterArt;
	}

	public void setMasterArt(String masterArt) {
		this.masterArt = masterArt;
	}

	public String getSlaveCode() {
		return slaveCode;
	}

	public void setSlaveCode(String slaveCode) {
		this.slaveCode = slaveCode;
	}

	public String getSlaveArt() {
		return slaveArt;
	}

	public void setSlaveArt(String slaveArt) {
		this.slaveArt = slaveArt;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public String getValidSide() {
		return validSide;
	}

	public void setValidSide(String validSide) {
		this.validSide = validSide;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTo() {
		return validTo;
	}

	public void setValidTo(LocalDate validTo) {
		this.validTo = validTo;
	}
}
