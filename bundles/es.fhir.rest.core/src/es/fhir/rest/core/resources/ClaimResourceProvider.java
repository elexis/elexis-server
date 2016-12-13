package es.fhir.rest.core.resources;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Claim;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Verrechnet;

@Component(immediate = true)
public class ClaimResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Claim.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Claim, List<Verrechnet>> getTransformer() {
		return (IFhirTransformer<Claim, List<Verrechnet>>) transformerRegistry.getTransformerFor(Claim.class,
				List.class);
	}

	@Create
	public MethodOutcome createClaim(@ResourceParam Claim claim) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<List<Verrechnet>> created = getTransformer().createLocalObject(claim);
		if (created.isPresent() && !created.get().isEmpty()) {
			outcome.setCreated(true);
		} else {
			throw new InternalErrorException("Creation failed");
		}
		return outcome;
	}
}
