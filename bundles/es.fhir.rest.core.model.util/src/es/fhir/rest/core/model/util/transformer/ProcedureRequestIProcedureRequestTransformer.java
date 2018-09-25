package es.fhir.rest.core.model.util.transformer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.ProcedureRequest;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.VersionedResource;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.model.util.transformer.helper.AbstractHelper;
import es.fhir.rest.core.model.util.transformer.helper.FindingsContentHelper;

@Component
public class ProcedureRequestIProcedureRequestTransformer
		implements IFhirTransformer<ProcedureRequest, IProcedureRequest> {

	private static Logger logger = LoggerFactory.getLogger(ProcedureRequestIProcedureRequestTransformer.class);

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFindingsService findingsService;

	@Reference
	private ICodingService codingService;

	private FindingsContentHelper contentHelper;

	
	public void activate() {
		contentHelper = new FindingsContentHelper();
	}

	@Override
	public Optional<ProcedureRequest> getFhirObject(IProcedureRequest localObject, Set<Include> includes) {
		Optional<IBaseResource> resource = contentHelper.getResource(localObject);
		if (resource.isPresent()) {
			return Optional.of((ProcedureRequest) resource.get());
		}
		return Optional.empty();
	}

	@Override
	public Optional<IProcedureRequest> getLocalObject(ProcedureRequest fhirObject) {
		if (fhirObject != null && fhirObject.getId() != null) {
			Optional<IProcedureRequest> existing =
				findingsService.findById(fhirObject.getId(), IProcedureRequest.class);
			if (existing.isPresent()) {
				return Optional.of(existing.get());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<IProcedureRequest> updateLocalObject(ProcedureRequest fhirObject, IProcedureRequest localObject) {
		return Optional.empty();
	}

	@Override
	public Optional<IProcedureRequest> createLocalObject(ProcedureRequest fhirObject) {
		IProcedureRequest iProcedureRequest = findingsService.create(IProcedureRequest.class);
		contentHelper.setResource(fhirObject, iProcedureRequest);
		if (fhirObject.getSubject() != null && fhirObject.getSubject().hasReference()) {
			String id = fhirObject.getSubject().getReferenceElement().getIdPart();
			Optional<IPatient> patient = modelService.load(id, IPatient.class);
			patient.ifPresent(k -> iProcedureRequest.setPatientId(id));
		}
		IEncounter iEncounter = null;
		if (fhirObject.getContext() != null && fhirObject.getContext().hasReference()) {
			String id = fhirObject.getContext().getReferenceElement().getIdPart();
			Optional<IEncounter> encounter = findingsService.findById(id, IEncounter.class);
			if (encounter.isPresent()) {
				iEncounter = encounter.get();
				iProcedureRequest.setEncounter(iEncounter);
			}
		}
		findingsService.saveFinding(iProcedureRequest);
		if (iEncounter != null) {
			writeBehandlungSoapText(iEncounter, fhirObject);
		}
		return Optional.of(iProcedureRequest);
	}

	private void writeBehandlungSoapText(IEncounter iEncounter, ProcedureRequest procedureRequest) {
		Optional<ch.elexis.core.model.IEncounter> behandlung = modelService.load(iEncounter.getConsultationId(), ch.elexis.core.model.IEncounter.class);
		behandlung.ifPresent(cons -> {
			Optional<LockInfo> lockInfo = AbstractHelper.acquireLock(cons);
			if (lockInfo.isPresent()) {
				String subjectivText = getSubjectiveText(iEncounter);
				String assessmentText = getAssessmentText(iEncounter);
				String procedureText = getProcedureText(behandlung.get());

				StringBuilder text = new StringBuilder();
				if (!subjectivText.isEmpty()) {
					text.append("A:\n" + subjectivText);
				}
				if (!assessmentText.isEmpty()) {
					if (text.length() > 0) {
						text.append("\n\n");
					}
					text.append("B:\n" + assessmentText);
				}
				if (!procedureText.isEmpty()) {
					if (text.length() > 0) {
						text.append("\n\n");
					}
					text.append("P:\n" + procedureText);
				}

				logger.debug("Updating SOAP text of cons [" + cons.getId() + "]\n" + text.toString());

				VersionedResource vResource = VersionedResource.load(null);
				vResource.update(text.toString(), "From FHIR");
				cons.setVersionedEntry(vResource);
				modelService.save(cons);
				AbstractHelper.releaseLock(lockInfo.get());
			}
		});
	}

	private String getProcedureText(ch.elexis.core.model.IEncounter behandlung) {
		StringBuilder ret = new StringBuilder();
		@SuppressWarnings("unchecked")
		List<IProcedureRequest> procedureRequests = (List<IProcedureRequest>) ((Object) findingsService
				.getConsultationsFindings(behandlung.getId(), IProcedureRequest.class));
		if (procedureRequests != null && !procedureRequests.isEmpty()) {
			for (IProcedureRequest iProcedureRequest : procedureRequests) {
				Optional<String> text = iProcedureRequest.getText();
				text.ifPresent(t -> {
					if (ret.length() > 0) {
						ret.append("\n");
					}
					ret.append(t);
				});
			}
		}
		return ret.toString();
	}

	private String getAssessmentText(IEncounter iEncounter) {
		List<ICondition> indication = iEncounter.getIndication();
		StringBuilder ret = new StringBuilder();
		for (ICondition iCondition : indication) {
			List<ICoding> coding = iCondition.getCoding();
			Optional<String> text = iCondition.getText();
			ConditionCategory category = iCondition.getCategory();
			if (category == ConditionCategory.PROBLEMLISTITEM) {
				boolean hasText = text.isPresent() && !text.get().isEmpty();
				if (ret.length() > 0) {
					ret.append("\n");
				}
				if (hasText) {
					ret.append(text.orElse(""));
				}
				if (coding != null && !coding.isEmpty()) {
					if (hasText) {
						ret.append(", ");
					}
					for (ICoding iCoding : coding) {
						ret.append(codingService.getLabel(iCoding));
					}
				}
			}
		}
		return ret.toString();
	}

	private String getSubjectiveText(IEncounter iEncounter) {
		List<IObservation> observations = findingsService
			.getConsultationsFindings(iEncounter.getConsultationId(),
				IObservation.class);
		StringBuilder ret = new StringBuilder();
		for (IObservation iObservation : observations) {
			if (iObservation.getCategory() == ObservationCategory.SOAP_SUBJECTIVE) {
				Optional<String> text = iObservation.getText();
				if (ret.length() > 0) {
					ret.append("\n");
				}
				ret.append(text.orElse(""));
			}
		}
		return ret.toString();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return ProcedureRequest.class.equals(fhirClazz) && IProcedureRequest.class.equals(localClazz);
	}

}
