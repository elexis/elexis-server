package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;

@Entity
@Table(name = "patient_artikel_joint")
public class Prescription extends AbstractDBObjectIdDeletedExtInfo {
	// TODO incomplete
	
	@Column(length = 3)
	private String anzahl;

	@Column
	@Convert(value = "ElexisDBStoreToStringConverter")
	private AbstractDBObject artikel;

	/**
	 * @deprecated store values in {@link #artikel}
	 */
	@Column(length = 25)
	private String artikelID;

	@Column(length = 255)
	private String bemerkung;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	private LocalDate dateFrom;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	private LocalDate dateUntil;

	@Column(length = 255)
	private String dosis;

	@OneToOne
	@JoinColumn(name = "patientID")
	private Kontakt patientID;

	@Column(length = 25)
	private String rezeptID;

	public String getAnzahl() {
		return anzahl;
	}

	public void setAnzahl(String anzahl) {
		this.anzahl = anzahl;
	}

	public AbstractDBObject getArtikel() {
		return artikel;
	}

	public void setArtikel(AbstractDBObject artikel) {
		this.artikel = artikel;
	}

	public String getArtikelID() {
		return artikelID;
	}

	public void setArtikelID(String artikelID) {
		this.artikelID = artikelID;
	}

	public String getBemerkung() {
		return bemerkung;
	}

	public void setBemerkung(String bemerkung) {
		this.bemerkung = bemerkung;
	}

	public LocalDate getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(LocalDate dateFrom) {
		this.dateFrom = dateFrom;
	}

	public LocalDate getDateUntil() {
		return dateUntil;
	}

	public void setDateUntil(LocalDate dateUntil) {
		this.dateUntil = dateUntil;
	}

	public String getDosis() {
		return dosis;
	}

	public void setDosis(String dosis) {
		this.dosis = dosis;
	}

	public Kontakt getPatientID() {
		return patientID;
	}

	public void setPatientID(Kontakt patientID) {
		this.patientID = patientID;
	}

	public String getRezeptID() {
		return rezeptID;
	}

	public void setRezeptID(String rezeptID) {
		this.rezeptID = rezeptID;
	}
}
