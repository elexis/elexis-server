package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_CLINICALIMPRESSION")
public class ClinicalImpression extends AbstractDBObjectIdDeleted {

	@Column(length = 80)
	private String patientid;

	@Column(length = 80)
	private String encounterid;

	@Lob
	private String content;

	public String getPatientid() {
		return patientid;
	}

	public void setPatientid(String patientid) {
		this.patientid = patientid;
	}

	public String getEncounterid() {
		return encounterid;
	}

	public void setEncounterid(String encounterid) {
		this.encounterid = encounterid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String getLabel() {
		return toString();
	}
}
