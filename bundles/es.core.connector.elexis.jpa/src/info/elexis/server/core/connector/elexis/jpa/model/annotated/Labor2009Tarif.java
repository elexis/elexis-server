package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.converter.ElexisDBStringDateConverter;

@Entity
@Table(name = "CH_MEDELEXIS_LABORTARIF2009")
public class Labor2009Tarif extends AbstractDBObjectIdDeleted {

	@Column(length = 255)
	private String chapter;

	@Column(length = 12)
	private String code;

	@Column(length = 10)
	private String tp;

	@Column(length = 255)
	private String name;

	@Lob
	private String limitatio;

	@Column(length = 10)
	private String fachbereich;

	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	private LocalDate gueltigVon;

	@Converter(name = "ElexisDBStringDateConverter", converterClass = ElexisDBStringDateConverter.class)
	@Convert("ElexisDBStringDateConverter")
	private LocalDate gueltigBis;

	@Column(length = 2)
	private String praxistyp;

	public String getChapter() {
		return chapter;
	}

	public void setChapter(String chapter) {
		this.chapter = chapter;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTp() {
		return tp;
	}

	public void setTp(String tp) {
		this.tp = tp;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLimitatio() {
		return limitatio;
	}

	public void setLimitatio(String limitatio) {
		this.limitatio = limitatio;
	}

	public String getFachbereich() {
		return fachbereich;
	}

	public void setFachbereich(String fachbereich) {
		this.fachbereich = fachbereich;
	}

	public LocalDate getGueltigVon() {
		return gueltigVon;
	}

	public void setGueltigVon(LocalDate gueltigVon) {
		this.gueltigVon = gueltigVon;
	}

	public LocalDate getGueltigBis() {
		return gueltigBis;
	}

	public void setGueltigBis(LocalDate gueltigBis) {
		this.gueltigBis = gueltigBis;
	}

	public String getPraxistyp() {
		return praxistyp;
	}

	public void setPraxistyp(String praxistyp) {
		this.praxistyp = praxistyp;
	}

}
