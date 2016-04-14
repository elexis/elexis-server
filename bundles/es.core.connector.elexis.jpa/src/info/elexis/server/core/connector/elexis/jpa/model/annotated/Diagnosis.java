package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "DIAGNOSEN")
public class Diagnosis extends AbstractDBObjectIdDeleted {

	@Column(length = 255, name = "DG_TXT")
	private String text;

	@Column(length = 25, name = "DG_CODE")
	private String code;

	@Column(length = 80, name = "KLASSE")
	private String diagnosisClass;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDiagnosisClass() {
		return diagnosisClass;
	}

	public void setDiagnosisClass(String diagnosisClass) {
		this.diagnosisClass = diagnosisClass;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Diagnosis)) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		Diagnosis other = (Diagnosis) obj;

		return (this.code.equals(other.code) && this.diagnosisClass.equals(other.diagnosisClass));
	}
}
