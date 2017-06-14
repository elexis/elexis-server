package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Practitioner;
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
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.UserService;

@Component(immediate = true)
public class PractitionerResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Practitioner.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Practitioner, Kontakt> getTransformer() {
		return (IFhirTransformer<Practitioner, Kontakt>) transformerRegistry.getTransformerFor(Practitioner.class,
				Kontakt.class);
	}

	@Read
	public Practitioner getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Kontakt> practitioner = KontaktService.load(idPart);
			if (practitioner.isPresent()) {
				if (practitioner.get().isMandator()) {
					Optional<Practitioner> fhirPractitioner = getTransformer().getFhirObject(practitioner.get());
					return fhirPractitioner.get();
				}
			}
		}
		return null;
	}

	@Search()
	public List<Practitioner> findPractitioner(@RequiredParam(name = Practitioner.SP_NAME) String name) {
		if (name != null) {
			JPAQuery<Kontakt> query = new JPAQuery<>(Kontakt.class);
			query.add(Kontakt_.description1, QUERY.LIKE, "%" + name + "%");
			query.or(Kontakt_.description2, QUERY.LIKE, "%" + name + "%");
			query.add(Kontakt_.mandator, QUERY.EQUALS, true);
			List<Kontakt> practitioners = query.execute();
			if (!practitioners.isEmpty()) {
				// only Kontakt with existing user entry
				practitioners = practitioners.stream().filter(kontakt -> UserService.findByKontakt(kontakt).isPresent())
						.collect(Collectors.toList());
				List<Practitioner> ret = new ArrayList<Practitioner>();
				for (Kontakt practitioner : practitioners) {
					Optional<Practitioner> fhirPractitioner = getTransformer().getFhirObject(practitioner);
					fhirPractitioner.ifPresent(fp -> ret.add(fp));
				}
				return ret;
			}
		}
		return Collections.emptyList();
	}
}
