package es.fhir.rest.core.resources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Task;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.Count;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.TokenParamModifier;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IReminderResponsibleLink;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ILocalLockService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.ISubQuery;

@Component(service = IFhirResourceProvider.class)
public class TaskResourceProvider extends AbstractFhirCrudResourceProvider<Task, IReminder> {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private ILocalLockService localLockService;

	@Reference
	private IContextService contextService;

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	public TaskResourceProvider() {
		super(IReminder.class);
	}

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Task.class;
	}

	@Activate
	public void activate() {
		super.setModelService(coreModelService);
		super.setContextService(contextService);
		super.setLocalLockService(localLockService);
	}

	@Override
	public IFhirTransformer<Task, IReminder> getTransformer() {
		return transformerRegistry.getTransformerFor(Task.class, IReminder.class);
	}

	@Search
	public List<Task> search(@OptionalParam(name = Task.SP_STATUS) TokenParam statusParam,
			@OptionalParam(name = Task.SP_OWNER) ReferenceParam theOwnerParam,
			@OptionalParam(name = Task.SP_PATIENT) ReferenceParam thePatientParam,
			@OptionalParam(name = Task.SP_CODE) TokenParam codeParam,
			@Count Integer theCount) {
		// always refresh cache data as change could have happened
		IQuery<IReminder> query = coreModelService.getQuery(IReminder.class, true, false);

		if (statusParam != null) {
			ProcessStatus processStatus= getProcessStatus(statusParam.getValue());
			if (processStatus == ProcessStatus.OPEN) {
				query.startGroup();
				query.or(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.EQUALS, processStatus);
				query.or(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.EQUALS, ProcessStatus.DUE);
				query.or(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.EQUALS, ProcessStatus.OVERDUE);
				query.andJoinGroups();
			} else {
				query.and(ModelPackage.Literals.IREMINDER__STATUS,
						hasNotModifier(statusParam) ? COMPARATOR.NOT_EQUALS : COMPARATOR.EQUALS, processStatus);
			}
		}
		if (codeParam != null) {
			if ("http://www.elexis.info/task/visibility".equals(codeParam.getSystem())) {
				// query for visibility with code popup query for all popup visibility
				if ("popup".equalsIgnoreCase(codeParam.getValue())) {
					if (hasNotModifier(codeParam)) {
						query.and("visibility", COMPARATOR.NOT_EQUALS, Visibility.POPUP_ON_PATIENT_SELECTION);
						query.and("visibility", COMPARATOR.NOT_EQUALS, Visibility.POPUP_ON_LOGIN);
					} else {
						query.startGroup();
						query.and("visibility", COMPARATOR.EQUALS, Visibility.POPUP_ON_PATIENT_SELECTION);
						query.or("visibility", COMPARATOR.EQUALS, Visibility.POPUP_ON_LOGIN);
						query.andJoinGroups();
					}
				} else {
					query.and("visibility", (hasNotModifier(codeParam) ? COMPARATOR.NOT_EQUALS : COMPARATOR.EQUALS),
							Visibility.valueOf(codeParam.getValue()));
				}
			}
		}

		if (theOwnerParam != null) {
			boolean ownerFound = false;
			if (theOwnerParam.getIdPart().equals("ALL")) {
				query.and("responsibleValue", COMPARATOR.EQUALS, "ALL");
				ownerFound = true;
			} else {
				Optional<IContact> ownerContact = coreModelService.load(theOwnerParam.getIdPart(), IContact.class);
				if (ownerContact.isPresent()) {
					ISubQuery<IReminderResponsibleLink> subQuery = query.createSubQuery(IReminderResponsibleLink.class,
							coreModelService);
					subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
					subQuery.and("responsible", COMPARATOR.EQUALS, ownerContact.get());
					query.exists(subQuery);
					ownerFound = true;
				} else {
					Optional<IUserGroup> userGroup = coreModelService.load(theOwnerParam.getIdPart(), IUserGroup.class);
					if (userGroup.isPresent()) {
						query.and("userGroup", COMPARATOR.EQUALS, userGroup.get());
						ownerFound = true;
					}
				}
			}
			// no need to query if no owner found
			if (!ownerFound) {
				return Collections.emptyList();
			}
		}

		if (thePatientParam != null) {
			Optional<IPatient> patientContact = coreModelService.load(thePatientParam.getIdPart(), IPatient.class);
			if (patientContact.isPresent()) {
				query.and(ModelPackage.Literals.IREMINDER__CONTACT, COMPARATOR.EQUALS, patientContact.get());
			} else {
				// no need to query if no patient found
				return Collections.emptyList();
			}
		}

		query.orderBy(ModelPackage.Literals.IREMINDER__DUE, ORDER.DESC);

		if (theCount != null) {
			// TODO only valid with theSort set, somehow combine?
			query.limit(theCount);
		}

		return super.handleExecute(query, null, null);
	}

	private boolean hasNotModifier(TokenParam statusParam) {
		return statusParam.getModifier() != null && statusParam.getModifier() == TokenParamModifier.NOT;
	}

	private ProcessStatus getProcessStatus(String string) {
		switch (string) {
		case "DRAFT":
		case "ACCEPTED":
		case "RECEIVED":
		case "READY":
		case "REQUESTED":
			return ProcessStatus.OPEN;
		case "COMPLETED":
		case "CANCELLED":
		case "FAILED":
		case "ENTEREDINERROR":
		case "REJECTED":
			return ProcessStatus.CLOSED;
		case "INPROGRESS":
			return ProcessStatus.IN_PROGRESS;
		case "ONHOLD":
			return ProcessStatus.ON_HOLD;
		default:
			break;
		}
		return ProcessStatus.OPEN;
	}
}