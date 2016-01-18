package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;

@Entity
@Table(name = "patient_artikel_joint")
public class Prescription extends AbstractDBObjectWithExtInfo {
	// TODO incomplete
	
	@Column(length = 3)
	private String anzahl;

	@Column(length = 255)
	private String artikel;

	/**
	 * @deprecated store values in {@link #artikel}
	 */
	@Column(length = 25)
	private String artikelID;

	@Column(length = 255)
	private String bemerkung;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	private Date dateFrom;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	private Date dateUntil;

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

	public String getArtikel() {
		return artikel;
	}

	public void setArtikel(String artikel) {
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

	public Date getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
	}

	public Date getDateUntil() {
		return dateUntil;
	}

	public void setDateUntil(Date dateUntil) {
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
