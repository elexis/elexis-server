package es.fhir.rest.core.resources;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.ValueSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.UriParam;
import ch.elexis.core.fhir.CodeSystem;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;
import ch.elexis.core.services.ICodeElementServiceContribution;

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
		return (IFhirTransformer<ValueSet, List>) transformerRegistry.getTransformerFor(ValueSet.class, List.class);
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
			Map<Object, Object> argumentsMap = new HashMap<Object, Object>(2);
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

		}

		return null;
	}

}
