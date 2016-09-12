package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.osgi.service.component.annotations.Component;

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

@Component
public class PractitionerResourceProvider implements IFhirResourceProvider {

	private IFhirTransformer<Practitioner, Kontakt> practitionerMapper;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Practitioner.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initTransformer(IFhirTransformerRegistry transformerRegistry) {
		practitionerMapper = (IFhirTransformer<Practitioner, Kontakt>) transformerRegistry
				.getTransformerFor(Practitioner.class, Kontakt.class);
		if (practitionerMapper == null) {
			throw new IllegalStateException("No transformer available");
		}
	}

	@Read
	public Practitioner getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Kontakt> practitioner = KontaktService.INSTANCE.findById(idPart);
			if (practitioner.isPresent()) {
				if (practitioner.get().isMandator()) {
					Optional<Practitioner> fhirPractitioner = practitionerMapper.getFhirObject(practitioner.get());
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
				List<Practitioner> ret = new ArrayList<Practitioner>();
				for (Kontakt practitioner : practitioners) {
					Optional<Practitioner> fhirPractitioner = practitionerMapper.getFhirObject(practitioner);
					fhirPractitioner.ifPresent(fp -> ret.add(fp));
				}
				return ret;
			}
		}
		return Collections.emptyList();
	}
}
