package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_OBSERVATION")
public class Observation extends AbstractDBObjectIdDeleted {

	@Column
	@Convert("booleanStringConverter")
	protected boolean referenced = false;

	@Column(length = 8)
	private String type;

	@Column(length = 80)
	private String patientid;

	@Column(length = 80)
	private String encounterid;

	@Column(length = 80)
	private String performerid;

	@Column(length = 255)
	private String originuri;

	@Column(length = 8)
	private String decimalplace;

	@Lob
	private String format;

	@Lob
	private String script;

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

	public String getOriginuri() {
		return originuri;
	}

	public void setOriginuri(String originuri) {
		this.originuri = originuri;
	}

	public String getDecimalplace() {
		return decimalplace;
	}

	public void setDecimalplace(String decimalplace) {
		this.decimalplace = decimalplace;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getScript() {
		return script;
	}

	public void setScript(String script) {
		this.script = script;
	}

	public boolean isReferenced() {
		return referenced;
	}

	public void setReferenced(boolean referenced) {
		this.referenced = referenced;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getLabel() {
		return toString();
	}
}
