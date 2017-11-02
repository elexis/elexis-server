package info.elexis.server.findings.fhir.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_OBSERVATIONLINK")
public class ObservationLink extends AbstractDBObjectIdDeleted {

	@Column(length = 80)
	private String sourceid;

	@Column(length = 80)
	private String targetid;

	@Column(length = 8)
	private String type;

	@Column(length = 255)
	private String description;

	public String getSourceid() {
		return sourceid;
	}

	public void setSourceid(String sourceid) {
		this.sourceid = sourceid;
	}

	public String getTargetid() {
		return targetid;
	}

	public void setTargetid(String targetid) {
		this.targetid = targetid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getLabel() {
		return toString();
	}
}
