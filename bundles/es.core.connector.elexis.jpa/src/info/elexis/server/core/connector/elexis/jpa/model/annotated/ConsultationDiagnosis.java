package info.elexis.server.core.connector.elexis.jpa.model.annotated;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "BEHDL_DG_JOINT")
public class ConsultationDiagnosis extends AbstractDBObjectIdDeleted {

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "BehandlungsID")
	private Behandlung consultation;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "DiagnoseID")
	private Diagnosis diagnosis;

	public Diagnosis getDiagnosis() {
		return diagnosis;
	}

	public void setDiagnosis(Diagnosis diagnosis) {
		this.diagnosis = diagnosis;
	}

	public Behandlung getConsultation() {
		return consultation;
	}

	public void setConsultation(Behandlung consultation) {
		this.consultation = consultation;
	}
}
