package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_OBSERVATION")
public class Observation extends AbstractDBObjectIdDeleted {

	@Column(length = 80)
	private String patientid;

	@Column(length = 80)
	private String encounterid;

	@Column(length = 80)
	private String performerid;

	@Lob
	private String content;

	public String getPatientId() {
		return patientid;
	}

	public void setPatientId(String patientId) {
		this.patientid = patientId;
	}

	public String getEncounterId() {
		return encounterid;
	}

	public void setEncounterId(String consultationId) {
		this.encounterid = consultationId;
	}

	public String getPerformerId() {
		return performerid;
	}

	public void setPerformerId(String performerId) {
		this.performerid = performerId;
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
