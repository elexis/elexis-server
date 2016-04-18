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

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.converter.ElexisDBStringDateConverter;

@Entity
@Table(name = "BRIEFE")
public class Brief extends AbstractDBObjectIdDeleted {

	@Column(length = 255, name = "betreff")
	protected String subject;
	
	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	protected LocalDate datum;
	
	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	protected LocalDate modifiziert;
	
	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	protected LocalDate gedruckt;
	
	@OneToOne
	@JoinColumn(name = "absenderID")
	protected Kontakt sender;
	
	@OneToOne
	@JoinColumn(name = "destID")
	protected Kontakt recipient;
	
	@OneToOne
	@JoinColumn(name = "patientID")
	protected Kontakt patient;
	
	@OneToOne
	@JoinColumn(name = "behandlungsID")
	protected Behandlung consultation;

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
	
	@Column
	@Convert("booleanStringConverter")
	protected boolean geloescht = false;

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
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

	public Kontakt getSender() {
		return sender;
	}

	public void setSender(Kontakt sender) {
		this.sender = sender;
	}

	public Kontakt getRecipient() {
		return recipient;
	}

	public Behandlung getConsultation() {
		return consultation;
	}
	
	public void setConsultation(Behandlung consultation) {
		this.consultation = consultation;
	}
	
	public void setRecipient(Kontakt recipient) {
		this.recipient = recipient;
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
	
	public void setContent(Heap content) {
		this.content = content;
	}
	
	public boolean isGeloescht() {
		return geloescht;
	}
	
	public void setGeloescht(boolean geloescht) {
		this.geloescht = geloescht;
	}
}
