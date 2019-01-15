package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.annotation.Update;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.resources.util.QueryUtil;
import info.elexis.server.core.connector.elexis.services.ContactService;

@Component
public class PatientResourceProvider implements IFhirResourceProvider {

	private Logger log;
	private ResourceProviderUtil resourceProviderUtil;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Patient.class;
	}

	@Activate
	public void activate() {
		log = LoggerFactory.getLogger(getClass());
		resourceProviderUtil = new ResourceProviderUtil();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Patient, IPatient> getTransformer() {
		return (IFhirTransformer<Patient, IPatient>) transformerRegistry.getTransformerFor(Patient.class,
				IPatient.class);
	}

	@Read
	public Patient getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IPatient> patient = modelService.load(idPart, IPatient.class);
			if (patient.isPresent() && patient.get().isPatient()) {
				Optional<Patient> fhirPatient = getTransformer().getFhirObject(patient.get());
				return fhirPatient.get();
			}
		}
		return null;
	}

	@Search()
	public List<Patient> findPatientByIdentifier(@RequiredParam(name = Patient.SP_IDENTIFIER) IdentifierDt identifier) {
		if (identifier != null && identifier.getSystem().equals(IdentifierSystem.ELEXIS_PATNR.getSystem())) {
			Optional<IPatient> patient = ContactService
					.findPatientByPatientNumber(Integer.valueOf(identifier.getValue()));
			if (patient.isPresent() && patient.get().isPatient()) {
				Optional<Patient> fhirPatient = getTransformer().getFhirObject(patient.get());
				return Collections.singletonList(fhirPatient.get());
			}
		}
		return Collections.emptyList();
	}

	@Search()
	public List<Patient> findPatient(@RequiredParam(name = Patient.SP_NAME) String name, @Sort SortSpec theSort) {
		if (name != null) {
			IQuery<IPatient> query = modelService.getQuery(IPatient.class);
			query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "%" + name + "%", true);
			query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE, "%" + name + "%", true);

			if (theSort != null) {
				String param = theSort.getParamName();
				SortOrderEnum order = theSort.getOrder();
				switch (param) {
				case Patient.SP_FAMILY:
					query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION1, QueryUtil.sortOrderEnumToLocal(order));
					break;
				case Patient.SP_GIVEN:
					query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION2, QueryUtil.sortOrderEnumToLocal(order));
					break;
				default:
					log.info("sortParamName [{}] not supported.", param);
					break;
				}
			}

			List<IPatient> patients = query.execute();
			if (!patients.isEmpty()) {
				List<Patient> ret = new ArrayList<>();
				for (IPatient patient : patients) {
					Optional<Patient> fhirPatient = getTransformer().getFhirObject(patient);
					fhirPatient.ifPresent(ret::add);
				}
				return ret;
			}
		}
		return Collections.emptyList();
	}

	@Create
	public MethodOutcome createPatient(@ResourceParam Patient patient) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<IPatient> created = getTransformer().createLocalObject(patient);
		if (created.isPresent()) {
			outcome.setCreated(true);
			outcome.setId(new IdDt("Patient", created.get().getId()));
			// TODO return the created object
		} else {
			throw new InternalErrorException("Creation failed");
		}
		return outcome;
	}
	
	@Update
	public MethodOutcome updatePatient(@IdParam IdType theId, @ResourceParam Patient patient) {		
		// TODO request lock
		return resourceProviderUtil.updateResource(theId, getTransformer(), patient, log);
	}

}
