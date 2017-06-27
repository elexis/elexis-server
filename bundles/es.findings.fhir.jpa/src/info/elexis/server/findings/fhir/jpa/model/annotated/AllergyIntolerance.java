package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;


@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_ALLERGYINTOLERANCE")
public class AllergyIntolerance extends AbstractDBObjectIdDeleted {

	@Column(length = 80)
	private String patientid;

	@Lob
	private String content;

	public String getPatientId() {
		return patientid;
	}

	public void setPatientId(String patientId) {
		this.patientid = patientId;
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
