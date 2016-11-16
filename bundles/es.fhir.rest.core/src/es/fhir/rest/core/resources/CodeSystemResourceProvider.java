package es.fhir.rest.core.resources;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.CodeSystem;
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
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;

@Component
public class CodeSystemResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return CodeSystem.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<CodeSystem, String> getTransformer() {
		return (IFhirTransformer<CodeSystem, String>) transformerRegistry.getTransformerFor(CodeSystem.class,
				String.class);
	}

	@Read
	public CodeSystem getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<CodeSystem> fhirCodeSystem = getTransformer().getFhirObject(idPart);
			if (fhirCodeSystem.isPresent()) {
				return fhirCodeSystem.get();
			}
		}
		return null;
	}

	@Search()
	public List<CodeSystem> findCodeSystem(@RequiredParam(name = CodeSystem.SP_SYSTEM) String system) {
		if (system != null) {
			Optional<CodeSystem> fhirCodeSystem = getTransformer().getFhirObject(system);
			if (fhirCodeSystem.isPresent()) {
				return Collections.singletonList(fhirCodeSystem.get());
			}
		}
		return Collections.emptyList();
	}
}
