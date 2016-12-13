package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Observation;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationCategory;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.resources.util.CodeTypeUtil;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult_;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.LabResultService;

@Component
public class ObservationResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Observation.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Observation, IObservation> getTransformer() {
		return (IFhirTransformer<Observation, IObservation>) transformerRegistry.getTransformerFor(Observation.class,
				IObservation.class);
	}

	@SuppressWarnings("unchecked")
	public IFhirTransformer<Observation, LabResult> getLabTransformer() {
		return (IFhirTransformer<Observation, LabResult>) transformerRegistry.getTransformerFor(Observation.class,
				LabResult.class);
	}

	@Read
	public Observation getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			// do lookup in findings first, then lab results
			Optional<IFinding> observation = findingsService.findById(idPart, IObservation.class);
			if (observation.isPresent() && (observation.get() instanceof IObservation)) {
				Optional<Observation> fhirObservation = getTransformer()
						.getFhirObject((IObservation) observation.get());
				return fhirObservation.get();
			}
			Optional<LabResult> labresult = LabResultService.INSTANCE.findById(theId);
			if (labresult.isPresent()) {
				Optional<Observation> fhirObservation = getLabTransformer().getFhirObject(labresult.get());
				return fhirObservation.get();
			}
		}
		return null;
	}

	@Search()
	public List<Observation> findObservation(@RequiredParam(name = Observation.SP_PATIENT) IdType thePatientId,
			@OptionalParam(name = Observation.SP_CATEGORY) CodeType categoryCode) {
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<Kontakt> patient = KontaktService.INSTANCE.findById(thePatientId.getIdPart());
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					List<Observation> ret = new ArrayList<Observation>();
					// laboratory
					if (categoryCode == null || ObservationCategory.LABORATORY.name()
							.equalsIgnoreCase(CodeTypeUtil.getCode(categoryCode).orElse(""))) {
						JPAQuery<LabResult> resultQuery = new JPAQuery<>(LabResult.class);
						resultQuery.add(LabResult_.patient, QUERY.EQUALS, patient);
						List<LabResult> results = resultQuery.execute();
						for (LabResult labResult : results) {

						}
					}
					// all other observations
					List<IFinding> findings = findingsService.getPatientsFindings(thePatientId.getIdPart(),
							IObservation.class);
					if (findings != null && !findings.isEmpty()) {
						for (IFinding iFinding : findings) {
							if (categoryCode != null && !isObservationCategory((IObservation) iFinding, categoryCode)) {
								continue;
							}
							Optional<Observation> fhirObservation = getTransformer()
									.getFhirObject((IObservation) iFinding);
							fhirObservation.ifPresent(fe -> ret.add(fe));
						}
						return ret;
					}
				}
			}
		}
		return Collections.emptyList();
	}

	private boolean isObservationCategory(IObservation iObservation, CodeType observationCode) {
		Optional<String> codeCode = CodeTypeUtil.getCode(observationCode);

		ObservationCategory category = iObservation.getCategory();
		return category.name().equalsIgnoreCase(codeCode.orElse("").replaceAll("-", ""));
	}
}
