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

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "CH_ELEXIS_OMNIVORE_DATA")
public class DocHandle extends AbstractDBObjectIdDeleted {

	@OneToOne
	@JoinColumn(name = "PatID")
	protected Kontakt kontakt;

	@Convert("ElexisDBStringDateConverter")
	protected LocalDate datum;

	@Column(length = 80)
	protected String category;

	@Column(length = 255)
	protected String title;

	@Column(length = 255)
	protected String mimetype;

	@Column(length = 512)
	protected String keywords;

	@Column(length = 255)
	protected String path;

	@Lob
	@Basic(fetch = FetchType.LAZY)
	protected byte[] doc;

	public Kontakt getKontakt() {
		return kontakt;
	}

	public void setKontakt(Kontakt kontakt) {
		this.kontakt = kontakt;
	}

	public LocalDate getDatum() {
		return datum;
	}

	public void setDatum(LocalDate datum) {
		this.datum = datum;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMimetype() {
		return mimetype;
	}

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public byte[] getDoc() {
		return doc;
	}

	public void setDoc(byte[] doc) {
		this.doc = doc;
	}
}
