package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_LOCALCODING")
public class LocalCoding extends AbstractDBObjectIdDeleted {

	@Column(length = 25)
	private String code;

	@Lob
	private String display;

	@Lob
	private String mapped;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public String getMapped() {
		return mapped;
	}

	public void setMapped(String mapped) {
		this.mapped = mapped;
	}

	@Override
	public String getLabel() {
		return toString();
	}
}
