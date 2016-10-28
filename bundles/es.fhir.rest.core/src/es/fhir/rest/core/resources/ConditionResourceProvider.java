package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Condition;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.migration.IFindingMigratorService;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.KontaktService;

@Component
public class ConditionResourceProvider implements IFhirResourceProvider {

	private IFindingMigratorService migratorService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingMigratorService(IFindingMigratorService migratorService) {
		this.migratorService = migratorService;
	}

	private IFindingsService findingsService;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFindingsService(IFindingsService findingsService) {
		this.findingsService = findingsService;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Condition.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Condition, ICondition> getTransformer() {
		return (IFhirTransformer<Condition, ICondition>) transformerRegistry
					.getTransformerFor(Condition.class, ICondition.class);
	}

	@Read
	public Condition getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IFinding> condition = findingsService.findById(idPart);
			if (condition.isPresent() && (condition.get() instanceof ICondition)) {
				Optional<Condition> fhirCondition = getTransformer().getFhirObject((ICondition) condition.get());
				return fhirCondition.get();
			}
		}
		return null;
	}

	@Search()
	public List<Condition> findCondition(@RequiredParam(name = Condition.SP_SUBJECT) IdType thePatientId) {
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<Kontakt> patient = KontaktService.INSTANCE.findById(thePatientId.getIdPart());
			if (patient.isPresent()) {
				if (patient.get().isPatient()) {
					// migrate diagnose condition first
					migratorService.migratePatientsFindings(thePatientId.getIdPart(), ICondition.class);

					List<IFinding> findings = findingsService.getPatientsFindings(thePatientId.getIdPart(),
							ICondition.class);
					if (findings != null && !findings.isEmpty()) {
						List<Condition> ret = new ArrayList<Condition>();
						for (IFinding iFinding : findings) {
							Optional<Condition> fhirEncounter = getTransformer().getFhirObject((ICondition) iFinding);
							fhirEncounter.ifPresent(fe -> ret.add(fe));
						}
						return ret;
					}
				}
			}
		}
		return null;
	}
}
