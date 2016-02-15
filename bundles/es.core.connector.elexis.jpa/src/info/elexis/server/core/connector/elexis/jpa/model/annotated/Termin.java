package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.ReadTransformer;
import org.eclipse.persistence.annotations.WriteTransformer;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer.ElexisDBStringDateTransformer;

@Entity
@Table(name = "AGNTERMINE")
public class Termin extends AbstractDBObjectIdDeleted {

	@OneToOne
	@JoinColumn(name = "PatID")
	private Kontakt patient;
	
	@Column(length = 25)
	private String bereich;

	@ReadTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	@WriteTransformer(transformerClass = ElexisDBStringDateTransformer.class)
	private LocalDate tag;
	
	@Column(length=4)
	private String beginn;
	
	@Column(length=4)
	private String dauer;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob()
	private String grund;
	
	@Column(length=50)
	private String terminTyp;
	
	@Column(length=50)
	private String terminStatus;
	
	@Column(length=25)
	private String erstelltVon;
	
	@Column(length=10)
	private String angelegt;
	
	@Column(length=10)
	private String lastedit;
	
	@Column
	private int palmId;
	
	@Column(length=10)
	private String flags;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob()
	private String extension;
	
	@Column(length=50)
	private String linkgroup;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob()
	private String statusHistory;

	public Kontakt getPatient() {
		return patient;
	}

	public void setPatient(Kontakt patient) {
		this.patient = patient;
	}

	public String getBereich() {
		return bereich;
	}

	public void setBereich(String bereich) {
		this.bereich = bereich;
	}

	public LocalDate getTag() {
		return tag;
	}

	public void setTag(LocalDate tag) {
		this.tag = tag;
	}

	public String getBeginn() {
		return beginn;
	}

	public void setBeginn(String beginn) {
		this.beginn = beginn;
	}

	public String getDauer() {
		return dauer;
	}

	public void setDauer(String dauer) {
		this.dauer = dauer;
	}

	public String getGrund() {
		return grund;
	}

	public void setGrund(String grund) {
		this.grund = grund;
	}

	public String getTerminTyp() {
		return terminTyp;
	}

	public void setTerminTyp(String terminTyp) {
		this.terminTyp = terminTyp;
	}

	public String getTerminStatus() {
		return terminStatus;
	}

	public void setTerminStatus(String terminStatus) {
		this.terminStatus = terminStatus;
	}

	public String getErstelltVon() {
		return erstelltVon;
	}

	public void setErstelltVon(String erstelltVon) {
		this.erstelltVon = erstelltVon;
	}

	public String getAngelegt() {
		return angelegt;
	}

	public void setAngelegt(String angelegt) {
		this.angelegt = angelegt;
	}

	public String getLastedit() {
		return lastedit;
	}

	public void setLastedit(String lastedit) {
		this.lastedit = lastedit;
	}

	public int getPalmId() {
		return palmId;
	}

	public void setPalmId(int palmId) {
		this.palmId = palmId;
	}

	public String getFlags() {
		return flags;
	}

	public void setFlags(String flags) {
		this.flags = flags;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getLinkgroup() {
		return linkgroup;
	}

	public void setLinkgroup(String linkgroup) {
		this.linkgroup = linkgroup;
	}

	public String getStatusHistory() {
		return statusHistory;
	}

	public void setStatusHistory(String statusHistory) {
		this.statusHistory = statusHistory;
	}
	
}
