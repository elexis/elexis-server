package es.fhir.rest.core.resources;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Claim;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ch.elexis.core.model.IBilled;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;

@Component
public class ClaimResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Claim.class;
	}

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Claim, List<IBilled>> getTransformer() {
		return (IFhirTransformer<Claim, List<IBilled>>) transformerRegistry.getTransformerFor(Claim.class,
				List.class);
	}

	@Create
	public MethodOutcome createClaim(@ResourceParam Claim claim) {
		MethodOutcome outcome = new MethodOutcome();
		Optional<List<IBilled>> created = getTransformer().createLocalObject(claim);
		if (created.isPresent() && !created.get().isEmpty()) {
			outcome.setCreated(true);
		} else {
			throw new InternalErrorException("Creation failed");
		}
		return outcome;
	}
}
