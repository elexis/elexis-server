package es.fhir.rest.core.model.util.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.hl7.fhir.dstu3.model.Claim;
import org.hl7.fhir.dstu3.model.Claim.CoverageComponent;
import org.hl7.fhir.dstu3.model.Claim.DiagnosisComponent;
import org.hl7.fhir.dstu3.model.Claim.ItemComponent;
import org.hl7.fhir.dstu3.model.Claim.SpecialConditionComponent;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.status.ObjectStatus;
import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.billable.IBillable;
import info.elexis.server.core.connector.elexis.billable.VerrechenbarTarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.ElexisTypeMap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Diagnosis;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.TarmedLeistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;
import info.elexis.server.core.connector.elexis.services.BehandlungService;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.TarmedLeistungService;

@Component
public class ClaimVerrechnetTransformer implements IFhirTransformer<Claim, List<Verrechnet>> {

	@Override
	public Optional<Claim> getFhirObject(List<Verrechnet> localObject) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<List<Verrechnet>> getLocalObject(Claim fhirObject) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<List<Verrechnet>> updateLocalObject(Claim fhirObject, List<Verrechnet> localObject) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<List<Verrechnet>> createLocalObject(Claim fhirObject) {
		List<CoverageComponent> coverages = fhirObject.getCoverage();
		List<ItemComponent> items = fhirObject.getItem();
		List<DiagnosisComponent> diagnosis = fhirObject.getDiagnosis();
		Reference providerRef = (Reference) fhirObject.getProvider();
		// test if all the information is present
		if (coverages != null && !coverages.isEmpty() && items != null && !items.isEmpty() && diagnosis != null
				&& !diagnosis.isEmpty() && providerRef != null && !providerRef.isEmpty()) {
			List<Verrechnet> ret = new ArrayList<>();
			Fall fall = getFall(coverages.get(0));
			List<IBillable<?>> billables = getBillable(items);
			List<Diagnosis> diagnose = getDiagnose(diagnosis);
			Behandlung behandlung = getOrCreateBehandlung(fhirObject, fall, providerRef);
			if (behandlung != null) {
				Optional<Kontakt> mandator = KontaktService.INSTANCE
						.findById(providerRef.getReferenceElement().getIdPart());
				if (mandator.isPresent()) {
					for (Diagnosis diag : diagnose) {
						BehandlungService.INSTANCE.setDiagnosisOnConsultation(behandlung, diag);
					}
					for (IBillable<?> billable : billables) {
						IStatus status = billable.add(behandlung, mandator.get(), mandator.get());
						if (status.isOK()) {
							ObjectStatus os = (ObjectStatus) status;
							ret.add((Verrechnet) os.getObject());
						}
					}
				}
			}
			return Optional.of(ret);
		} else {
			LoggerFactory.getLogger(ClaimVerrechnetTransformer.class)
					.warn("Could not create claim for coverrage [" + coverages + "] item [" + items + "] diagnosis ["
							+ diagnosis + "] provider [" + providerRef + "]");
		}
		return Optional.empty();
	}

	private Behandlung getOrCreateBehandlung(Claim fhirObject, Fall fall, Reference providerRef) {
		Behandlung ret = null;
		if (fhirObject.getInformation() != null && !fhirObject.getInformation().isEmpty()) {
			List<SpecialConditionComponent> information = fhirObject.getInformation();
			for (SpecialConditionComponent specialConditionComponent : information) {
				Type value = specialConditionComponent.getValue();
				if (value instanceof StringType) {
					if (((StringType) value).getValue().startsWith("Encounter/")) {
						IdType ref = new IdType(((StringType) value).getValue());
						if (ref.getIdPart() != null) {
							Optional<Behandlung> behandlung = BehandlungService.INSTANCE.findById(ref.getIdPart());
							if (behandlung.isPresent()) {
								ret = behandlung.get();
							}
						}
					}
				}
			}
		}
		// create if not available in the information
		if (ret == null) {
			Optional<Kontakt> mandator = KontaktService.INSTANCE
					.findById(providerRef.getReferenceElement().getIdPart());
			if (mandator.isPresent()) {
				ret = BehandlungService.INSTANCE.create(fall, mandator.get());
			}
		}
		return ret;
	}

	private List<Diagnosis> getDiagnose(List<DiagnosisComponent> diagnosis) {
		List<Diagnosis> ret = new ArrayList<>();
		for (DiagnosisComponent diagnosisComponent : diagnosis) {
			Coding coding = diagnosisComponent.getDiagnosis();
			if (coding != null) {
				Diagnosis diag = new Diagnosis();
				diag.setCode(coding.getCode());
				diag.setText((coding.getDisplay() != null) ? coding.getDisplay() : "MISSING");
				if (CodingSystem.ELEXIS_DIAGNOSE_TESSINERCODE.getSystem().equals(coding.getSystem())) {
					diag.setDiagnosisClass(ElexisTypeMap.TYPE_TESSINER_CODE);
				}
				ret.add(diag);
			}
		}
		return ret;
	}

	private List<IBillable<?>> getBillable(List<ItemComponent> items) {
		List<IBillable<?>> ret = new ArrayList<>();
		for (ItemComponent itemComponent : items) {
			Coding serviceCoding = itemComponent.getService();
			if (serviceCoding != null) {
				Optional<IBillable<?>> billable = getBillable(serviceCoding.getSystem(), serviceCoding.getCode());
				billable.ifPresent(b -> ret.add(b));
			}
		}
		return ret;
	}

	private Optional<IBillable<?>> getBillable(String system, String code) {
		if (system.equals(CodingSystem.ELEXIS_TARMED_CODESYSTEM.getSystem())) {
			Optional<TarmedLeistung> tarmed = TarmedLeistungService.findFromCode(code, null);
			if (tarmed.isPresent()) {
				return Optional.of(new VerrechenbarTarmedLeistung(tarmed.get()));
			}
		}
		LoggerFactory.getLogger(ClaimVerrechnetTransformer.class)
				.warn("Could not find billable for system [" + system + "] code [" + code + "]");
		return Optional.empty();
	}

	private Fall getFall(CoverageComponent coverageComponent) {
		Reference reference = (Reference) coverageComponent.getCoverage();
		if (reference != null && !reference.isEmpty()) {
			Optional<Fall> fallOpt = FallService.INSTANCE.findById(reference.getReferenceElement().getIdPart());
			return fallOpt.orElse(null);
		}
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return Claim.class.equals(fhirClazz) && List.class.equals(localClazz);
	}
}
