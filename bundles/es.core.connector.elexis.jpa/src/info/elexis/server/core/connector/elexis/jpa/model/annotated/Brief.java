package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;

@Entity
@Table(name = "BRIEFE")
public class Brief extends AbstractDBObject {

	@Column(length = 80)
	protected String betreff;
	
	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	protected LocalDate datum;
	
	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	protected LocalDate modifiziert;
	
	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	protected LocalDate gedruckt;
	
	@OneToOne
	@JoinColumn(name = "absenderID")
	protected Kontakt absender;
	
	@OneToOne
	@JoinColumn(name = "destID")
	protected Kontakt empfaenger;
	
	@OneToOne
	@JoinColumn(name = "patientID")
	protected Kontakt patient;
	
//	@OneToOne
//	@JoinColumn(name = "behandlungsID")
//	protected Object behandlung;

	@Column(length = 30)
	protected String typ;
	
	@Column(length = 80)
	protected String mimetype;
	
	@OneToOne
	@PrimaryKeyJoinColumn
	protected Heap content;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob()
	protected String path;

	public String getBetreff() {
		return betreff;
	}

	public void setBetreff(String betreff) {
		this.betreff = betreff;
	}

	public LocalDate getDatum() {
		return datum;
	}

	public void setDatum(LocalDate datum) {
		this.datum = datum;
	}

	public LocalDate getModifiziert() {
		return modifiziert;
	}

	public void setModifiziert(LocalDate modifiziert) {
		this.modifiziert = modifiziert;
	}

	public LocalDate getGedruckt() {
		return gedruckt;
	}

	public void setGedruckt(LocalDate gedruckt) {
		this.gedruckt = gedruckt;
	}

	public Kontakt getAbsender() {
		return absender;
	}

	public void setAbsender(Kontakt absender) {
		this.absender = absender;
	}

	public Kontakt getEmpfaenger() {
		return empfaenger;
	}

	public void setEmpfaenger(Kontakt empfaenger) {
		this.empfaenger = empfaenger;
	}

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public String getTyp() {
		return typ;
	}

	public void setTyp(String typ) {
		this.typ = typ;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
	
	public Heap getContent() {
		return content;
	}
}
