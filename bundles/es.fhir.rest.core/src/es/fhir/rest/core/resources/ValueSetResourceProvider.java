package es.fhir.rest.core.resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.ValueSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ch.elexis.core.fhir.CodeSystem;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.ICategory;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;
import ch.elexis.core.services.IDocumentStore;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.utils.OsgiServiceUtil;

@SuppressWarnings("rawtypes")
@Component
public class ValueSetResourceProvider implements IFhirResourceProvider<ValueSet, List> {

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Reference
	private ICodeElementService codeElementService;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return ValueSet.class;
	}

	@Override
	public IFhirTransformer<ValueSet, List> getTransformer() {
		return transformerRegistry.getTransformerFor(ValueSet.class, List.class);
	}

	@Search
	public ValueSet search(@RequiredParam(name = ValueSet.SP_URL) UriParam urlParam,
			@OptionalParam(name = "_text") StringParam textParam) {

		if (!StringUtils.startsWith(urlParam.getValue(), CodeSystem.CODEELEMENT.getUrl())) {
			return null;
		}

		// TODO useContext (law, date etc)
		String codeElementTypAndcodeSystemName = StringUtils.substring(urlParam.getValue(),
				CodeSystem.CODEELEMENT.getUrl().length() + 1);
		String[] codes = codeElementTypAndcodeSystemName.split("/");
		CodeElementTyp codeElementType = CodeElementTyp.valueOf(codes[0].toUpperCase());
		Optional<ICodeElementServiceContribution> contribution = codeElementService.getContribution(codeElementType,
				codes[1]);

		if (contribution.isPresent()) {
			Map<Object, Object> argumentsMap = new HashMap<>(2);
			if (codes.length > 2) {
				argumentsMap.put("path", String.join("/", Arrays.copyOfRange(codes, 2, codes.length)));
			}
			if (textParam != null) {
				argumentsMap.put(ICodeElementServiceContribution.CONTEXT_KEYS.DISPLAY, textParam.getValue());
			}
			List<ICodeElement> elements = contribution.get().getElements(argumentsMap);
			if (!elements.isEmpty()) {
				ValueSet valueSet = getTransformer().getFhirObject(elements).get();
				valueSet.getCompose().getInclude().get(0).setSystem(urlParam.getValue());
				return valueSet;
			}

		} else {
			LoggerFactory.getLogger(getClass()).warn("No contribution present for [{}]",
					codeElementTypAndcodeSystemName);
		}

		return null;
	}

	/**
	 * Create/Update/Delete a code in an existing ValueSet<br>
	 * Partial support: Only ValueSet for Omnivore Document Store category CRUD
	 * operations supported
	 * 
	 * @param urlParam
	 * @param operation "create", "update" or "delete"
	 * @param code      the code name to apply the operation to, if operation
	 *                  "create" this is <code>null</code>
	 * @param opParam   for "update" the new code name, for "delete" (optionally)
	 *                  the target code to move documents to
	 * @return
	 */
	@Operation(name = "$updateCodeInValueSet", idempotent = true)
	public OperationOutcome opUpdateCodeInValueSet(@OperationParam(name = ValueSet.SP_URL) UriParam urlParam,
			@OperationParam(name = "_op") StringParam operation, @OperationParam(name = "_code") StringParam code,
			@OperationParam(name = "_opParam") StringParam opParam) {

		if (urlParam == null || operation == null) {
			throw new InvalidRequestException("missing parameter");
		}

		IStatus status = Status.OK_STATUS;
		if ("http://elexis.info/codeelement/config/document-categories".equals(urlParam.getValue())) {
			IDocumentStore documentStore = OsgiServiceUtil
					.getService(IDocumentStore.class, "(storeid=ch.elexis.data.store.omnivore)").orElse(null);
			if (documentStore != null) {
				try {
					if ("create".equals(operation.getValue())) {
						if (opParam == null) {
							throw new InvalidRequestException("missing parameter _opParam");
						}
						documentStore.createCategory(opParam.getValue());
					} else {
						Optional<ICategory> foundCategory = documentStore.getCategoryByName(code.getValue());
						if (foundCategory.isPresent()) {
							if ("update".equals(operation.getValue())) {
								if (opParam == null || StringUtils.isBlank(opParam.getValue())) {
									throw new InvalidRequestException("missing or invalid _opUpdateParam parameter");
								}
								documentStore.renameCategory(foundCategory.get(), opParam.getValue());
							} else if ("delete".equals(operation.getValue())) {
								Optional<ICategory> targetCategory = opParam != null
										? documentStore.getCategoryByName(opParam.getValue())
										: Optional.empty();
								documentStore.removeCategory(foundCategory.get(), targetCategory.orElse(null));
							}
						}
					}
				} finally {
					OsgiServiceUtil.ungetService(documentStore);
				}
				status = ObjectStatus.OK_STATUS(search(urlParam, null));
			} else {
				LoggerFactory.getLogger(getClass()).warn("Could not getService() omnivore IDocumentStore");
				status = Status.error("internal service error");
			}
		} else {
			throw new InvalidRequestException("not-supported");
		}
		OperationOutcome operationOutcome = ResourceProviderUtil.statusToOperationOutcome(status);
		if (status instanceof ObjectStatus objectStatus) {
			Object object = objectStatus.getObject();
			if (object instanceof ValueSet valueSet) {
				operationOutcome.addContained(valueSet);
			}
		}
		return operationOutcome;
	}

}
