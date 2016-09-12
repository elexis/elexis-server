package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
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
public class OrganizationResourceProvider implements IFhirResourceProvider {

	private IFhirTransformer<Organization, Kontakt> organizationMapper;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Organization.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initTransformer(IFhirTransformerRegistry transformerRegistry) {
		organizationMapper = (IFhirTransformer<Organization, Kontakt>) transformerRegistry
				.getTransformerFor(Organization.class, Kontakt.class);
		if (organizationMapper == null) {
			throw new IllegalStateException("No transformer available");
		}
	}

	@Read
	public Organization getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Kontakt> organization = KontaktService.INSTANCE.findById(idPart);
			if (organization.isPresent()) {
				if (organization.get().isOrganisation()) {
					Optional<Organization> fhirOrganization = organizationMapper.getFhirObject(organization.get());
					return fhirOrganization.get();
				}
			}
		}
		return null;
	}

	@Search()
	public List<Organization> findOrganization(@RequiredParam(name = Organization.SP_NAME) String name) {
		if (name != null) {
			JPAQuery<Kontakt> query = new JPAQuery<>(Kontakt.class);
			query.add(Kontakt_.description1, QUERY.LIKE, "%" + name + "%");
			query.or(Kontakt_.description2, QUERY.LIKE, "%" + name + "%");
			query.add(Kontakt_.organisation, QUERY.EQUALS, true);
			List<Kontakt> organizations = query.execute();
			if (!organizations.isEmpty()) {
				List<Organization> ret = new ArrayList<Organization>();
				for (Kontakt organization : organizations) {
					Optional<Organization> fhirOrganization = organizationMapper.getFhirObject(organization);
					fhirOrganization.ifPresent(fp -> ret.add(fp));
				}
				return ret;
			}
		}
		return Collections.emptyList();
	}
}
