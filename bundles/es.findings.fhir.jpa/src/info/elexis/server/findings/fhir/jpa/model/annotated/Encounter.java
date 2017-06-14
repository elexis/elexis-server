package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;


@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_ENCOUNTER")
public class Encounter extends AbstractDBObjectIdDeleted {

	@Column(length = 80)
	private String patientid;

	@Column(length = 80)
	private String mandatorid;

	@Column(length = 80)
	private String consultationid;

	@Lob
	private String content;

	public String getPatientId() {
		return patientid;
	}

	public void setPatientId(String patientId) {
		this.patientid = patientId;
	}

	public String getConsultationId() {
		return consultationid;
	}

	public void setConsultationId(String consultationId) {
		this.consultationid = consultationId;
	}

	public String getMandatorId() {
		return mandatorid;
	}

	public void setMandatorId(String serviceProviderId) {
		this.mandatorid = serviceProviderId;
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
