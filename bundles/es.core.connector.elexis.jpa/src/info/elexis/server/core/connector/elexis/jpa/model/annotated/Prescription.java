package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDateTime;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTimeTransformer;

@Entity
@Table(name = "patient_artikel_joint")
public class Prescription extends AbstractDBObjectIdDeletedExtInfo {

	@Column(length = 3)
	private String anzahl;

	@Basic(fetch = FetchType.LAZY)
	@Column
	@Convert(value = "ElexisDBStoreToStringConverter")
	private AbstractDBObjectIdDeleted artikel;

	@Column(length = 255)
	private String bemerkung;

	@ReadTransformer(transformerClass = ElexisDBStringDateTimeTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTimeTransformer.class)
	private LocalDateTime dateFrom;

	@ReadTransformer(transformerClass = ElexisDBStringDateTimeTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTimeTransformer.class)
	private LocalDateTime dateUntil;

	@Column(length = 255)
	private String dosis;
	
	@Column(length = 2, name = "prescType")
	private String prescriptionType;

	@OneToOne
	@JoinColumn(name = "patientID")
	private Kontakt patient;

	@Column(length = 25)
	private String rezeptID;

	public String getAnzahl() {
		return anzahl;
	}

	public void setAnzahl(String anzahl) {
		this.anzahl = anzahl;
	}

	public AbstractDBObjectIdDeleted getArtikel() {
		return artikel;
	}

	public void setArtikel(AbstractDBObjectIdDeleted artikel) {
		this.artikel = artikel;
	}

	public String getBemerkung() {
		return bemerkung;
	}

	public void setBemerkung(String bemerkung) {
		this.bemerkung = bemerkung;
	}

	public LocalDateTime getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDateTime dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDateTime getDateUntil() {
		return dateUntil;
	}

	public void setDateUntil(LocalDateTime dateUntil) {
		this.dateUntil = dateUntil;
	}
	
	public String getPrescriptionType() {
		return prescriptionType;
	}
	
	public void setPrescriptionType(String prescriptionType) {
		this.prescriptionType = prescriptionType;
	}

	public String getDosis() {
		return dosis;
	}

	public void setDosis(String dosis) {
		this.dosis = dosis;
	}

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public String getRezeptID() {
		return rezeptID;
	}

	public void setRezeptID(String rezeptID) {
		this.rezeptID = rezeptID;
	}
}
