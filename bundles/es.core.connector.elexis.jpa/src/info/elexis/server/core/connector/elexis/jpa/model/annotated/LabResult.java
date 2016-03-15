package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;

@Entity
@Table(name = "laborwerte")
public class LabResult extends AbstractDBObjectIdDeletedExtInfo {

	@OneToOne
	@JoinColumn(name = "PatientID")
	private Kontakt patient;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	private LocalDate datum;

	@Column(length = 6)
	private String zeit;

	@OneToOne
	@JoinColumn(name = "ItemID")
	private LabItem item;
	
	@Column(length = 255)
	private String resultat;
	
	@Column(length = 10)
	private String flags;
	
	@Column(length = 30)
	private String origin;
	
	@Lob
	private String comment;
	
	@Column(length = 255)
	private String unit;
	
	@Column(length = 24)
	private String analysetime;
	
	@Column(length = 24)
	private String observationtime;
	
	@Column(length = 24)
	private String transmissiontime;
	
	@Column(length = 255)
	private String refMale;
	
	@Column(length = 255)
	private String refFemale;
	
	@Column(length = 25)
	private String originId;

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public LocalDate getDatum() {
		return datum;
	}

	public void setDatum(LocalDate datum) {
		this.datum = datum;
	}

	public String getZeit() {
		return zeit;
	}

	public void setZeit(String zeit) {
		this.zeit = zeit;
	}

	public LabItem getItem() {
		return item;
	}

	public void setItem(LabItem item) {
		this.item = item;
	}

	public String getResultat() {
		return resultat;
	}

	public void setResultat(String resultat) {
		this.resultat = resultat;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getAnalysetime() {
		return analysetime;
	}

	public void setAnalysetime(String analysetime) {
		this.analysetime = analysetime;
	}

	public String getObservationtime() {
		return observationtime;
	}

	public void setObservationtime(String observationtime) {
		this.observationtime = observationtime;
	}

	public String getTransmissiontime() {
		return transmissiontime;
	}

	public void setTransmissiontime(String transmissiontime) {
		this.transmissiontime = transmissiontime;
	}

	public String getRefMale() {
		return refMale;
	}

	public void setRefMale(String refMale) {
		this.refMale = refMale;
	}

	public String getRefFemale() {
		return refFemale;
	}

	public void setRefFemale(String refFemale) {
		this.refFemale = refFemale;
	}

	public String getOriginId() {
		return originId;
	}

	public void setOriginId(String originId) {
		this.originId = originId;
	}
}
