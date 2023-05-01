package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ch.elexis.core.fhir.FhirConstants;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.util.CodeTypeUtil;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISickCertificate;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

@Component(service = IFhirResourceProvider.class)
public class ConditionResourceProvider extends AbstractFhirCrudResourceProvider<Condition, ICondition> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IMigratorService migratorService;

	@Reference
	private IFindingsService findingsService;

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Reference
	private ILocalLockService localLockService;

	public ConditionResourceProvider() {
		super(ICondition.class);
	}
	
	@Activate
	public void activate() {
		super.setModelService(coreModelService);
		super.setLocalLockService(localLockService);
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Condition.class;
	}

	@Override
	public IFhirTransformer<Condition, ICondition> getTransformer() {
		return (IFhirTransformer<Condition, ICondition>) transformerRegistry.getTransformerFor(Condition.class,
				ICondition.class);
	}

	@Override
	@Read
	public Condition read(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<ICondition> condition = findingsService.findById(idPart, ICondition.class);
			if (condition.isPresent()) {
				Optional<Condition> fhirCondition = getTransformer().getFhirObject(condition.get());
				return fhirCondition.get();
			} else {
				Optional<ISickCertificate> sickCertificate = coreModelService.load(idPart, ISickCertificate.class);
				if (sickCertificate.isPresent()) {
					return getSickCertificateTransformer().getFhirObject(sickCertificate.get()).get();
				}
			}
		}
		return null;
	}

	@Search
	public List<Condition> findCondition(@OptionalParam(name = Encounter.SP_PATIENT) IdType thePatientId,
			@OptionalParam(name = Encounter.SP_SUBJECT) IdType theSubjectId,
			@OptionalParam(name = Condition.SP_CATEGORY) CodeType categoryCode,
			@OptionalParam(name = Condition.SP_CODE) TokenParam theCode) {
		if (thePatientId == null && theSubjectId != null) {
			thePatientId = theSubjectId;
		}
		
		if (thePatientId != null && !thePatientId.isEmpty()) {
			Optional<IPatient> patient = coreModelService.load(thePatientId.getIdPart(), IPatient.class);
			if (patient.isPresent()) {

				if (isAUF(theCode)) {
					IFhirTransformer<Condition, ISickCertificate> transformer = getSickCertificateTransformer();
					IQuery<ISickCertificate> query = coreModelService.getQuery(ISickCertificate.class);
					query.and(ModelPackage.Literals.ISICK_CERTIFICATE__PATIENT, COMPARATOR.EQUALS, patient.get());

					List<Condition> collect = query.execute().stream().map(isc -> transformer.getFhirObject(isc).get())
							.collect(Collectors.toList());
					return collect;
				}

				if (patient.get().isPatient()) {
					// migrate diagnose condition first
					migratorService.migratePatientsFindings(thePatientId.getIdPart(), ICondition.class, null);

					List<ICondition> findings = findingsService.getPatientsFindings(thePatientId.getIdPart(),
							ICondition.class);
					if (findings != null && !findings.isEmpty()) {
						List<Condition> ret = new ArrayList<Condition>();
						for (ICondition iFinding : findings) {
							if (categoryCode != null && !isConditionCategory(iFinding, categoryCode)) {
								continue;
							}
							Optional<Condition> fhirEncounter = getTransformer().getFhirObject(iFinding);
							fhirEncounter.ifPresent(fe -> ret.add(fe));
						}
						return ret;
					}
				}
			}
		}
		return Collections.emptyList();
	}

	private boolean isAUF(TokenParam theCode) {
		if (theCode != null && theCode.getSystem() != null && theCode.getValue() != null) {
			return FhirConstants.DE_EAU_SYSTEM.equals(theCode.getSystem())
					&& FhirConstants.DE_EAU_SYSTEM_CODE.equals(theCode.getValue());
		}
		return false;
	}

	@Override
	public MethodOutcome update(IdType theId, Condition fhirObject) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<ICondition> elexisCondition = getTransformer().getLocalObject(fhirObject);
		Optional<ISickCertificate> elexisSickCertificate = getSickCertificateTransformer().getLocalObject(fhirObject);
		if (elexisCondition.isPresent()) {
			checkMatchVersion(elexisCondition.get().getLastupdate(), fhirObject);

			LockResponse lockResponse = localLockService.acquireLock(elexisCondition.get());
			if (lockResponse.isOk()) {
				outcome = resourceProviderUtil.updateResource(theId, getTransformer(), fhirObject, log);
				localLockService.releaseLock(lockResponse);
			} else {
				throw new PreconditionFailedException("Could not acquire update lock");
			}
		} else if (elexisSickCertificate.isPresent()) {
			checkMatchVersion(elexisSickCertificate.get().getLastupdate(), fhirObject);

			LockResponse lockResponse = localLockService.acquireLock(elexisSickCertificate.get());
			if (lockResponse.isOk()) {
				outcome = resourceProviderUtil.updateResource(theId, getSickCertificateTransformer(), fhirObject, log);
				localLockService.releaseLock(lockResponse);
			} else {
				throw new PreconditionFailedException("Could not acquire update lock");
			}
		} else {
			log.warn("Could not find local object for [" + fhirObject + "] with id [" + fhirObject.getId() + "]");
			outcome = create(fhirObject);
		}
		return outcome;
	}
	
	@Override
	@Create
	public MethodOutcome create(@ResourceParam Condition condition) {
		MethodOutcome outcome = new MethodOutcome();

		if (condition.getCode().hasCoding(FhirConstants.DE_EAU_SYSTEM, FhirConstants.DE_EAU_SYSTEM_CODE)) {
			// eAUF
			Optional<ISickCertificate> exists = getSickCertificateTransformer().getLocalObject(condition);
			if (exists.isPresent()) {
				outcome.setCreated(false);
				outcome.setId(new IdType(condition.getId()));
			} else {
				Optional<ISickCertificate> created = getSickCertificateTransformer().createLocalObject(condition);
				if (created.isPresent()) {
					outcome.setCreated(true);
					outcome.setId(new IdType(created.get().getId()));
				} else {
					throw new InternalErrorException("Creation failed");
				}
			}

			return outcome;
		}

		Optional<ICondition> exists = getTransformer().getLocalObject(condition);
		if (exists.isPresent()) {
			outcome.setCreated(false);
			outcome.setId(new IdType(condition.getId()));
		} else {
			Optional<ICondition> created = getTransformer().createLocalObject(condition);
			if (created.isPresent()) {
				outcome.setCreated(true);
				outcome.setId(new IdType(created.get().getId()));
			} else {
				throw new InternalErrorException("Creation failed");
			}
		}
		return outcome;
	}
	
	private boolean isConditionCategory(ICondition iCondition, CodeType categoryCode) {
		Optional<String> codeCode = CodeTypeUtil.getCode(categoryCode);

		ConditionCategory category = iCondition.getCategory();
		return category.name().equalsIgnoreCase(codeCode.orElse(""))
				|| category.getCode().equalsIgnoreCase(codeCode.orElse(""));
	}

	private IFhirTransformer<Condition, ISickCertificate> getSickCertificateTransformer() {
		return transformerRegistry.getTransformerFor(Condition.class, ISickCertificate.class);
	}
}
