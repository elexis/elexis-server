package info.elexis.server.findings.fhir.jpa.model.annotated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IEncounter;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.findings.fhir.jpa.model.helper.FhirHelper;

@Entity
@Table(name = "CH_ELEXIS_CORE_FINDINGS_ENCOUNTER")
public class Encounter extends AbstractDBObjectIdDeleted implements IEncounter {

	@Transient
	private FhirHelper fhirHelper = new FhirHelper();

	@Column(length = 80)
	private String patientid;

	@Column(length = 80)
	private String serviceproviderid;

	@Column(length = 80)
	private String consultationid;

	@Lob
	private String content;

	@Override
	public String getPatientId() {
		return patientid;
	}

	@Override
	public void setPatientId(String patientId) {
		this.patientid = patientId;
	}

	@Override
	public String getConsultationId() {
		return consultationid;
	}

	@Override
	public void setConsultationId(String consultationId) {
		this.consultationid = consultationId;
	}

	@Override
	public String getServiceProviderId() {
		return serviceproviderid;
	}

	@Override
	public void setServiceProviderId(String serviceProviderId) {
		this.serviceproviderid = serviceProviderId;
	}

	@Override
	public List<ICoding> getCoding() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCoding(ICoding coding) {
		// TODO Auto-generated method stub

	}

	@Override
	public Optional<LocalDateTime> getEffectiveTime() {
		Optional<IBaseResource> resource = fhirHelper.loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			Period period = fhirEncounter.getPeriod();
			if (period != null && period.getStart() != null) {
				return Optional.of(getLocalDateTime(period.getStart()));
			}
		}
		return Optional.empty();
	}

	@Override
	public void setEffectiveTime(LocalDateTime time) {
		Optional<IBaseResource> resource = fhirHelper.loadResource(this);
		if (resource.isPresent()) {
			org.hl7.fhir.dstu3.model.Encounter fhirEncounter = (org.hl7.fhir.dstu3.model.Encounter) resource.get();
			Period period = fhirEncounter.getPeriod();
			if (period == null) {
				period = new Period();
				period.setStart(getDate(time));
			} else {
				period.setStart(getDate(time));
			}
			fhirEncounter.setPeriod(period);
		}
		fhirHelper.saveResource(resource.get(), this);
	}

	@Override
	public Optional<String> getText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RawContentFormat getRawContentFormat() {
		return RawContentFormat.FHIR_JSON;
	}

	@Override
	public String getRawContent() {
		return content;
	}

	@Override
	public void setRawContent(String content) {
		this.content = content;
	}
}
