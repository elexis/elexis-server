package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
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

@Component
public class OrganizationResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Organization.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Organization, Kontakt> getTransformer() {
		return (IFhirTransformer<Organization, Kontakt>) transformerRegistry
					.getTransformerFor(Organization.class, Kontakt.class);
	}

	@Read
	public Organization getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<Kontakt> organization = KontaktService.load(idPart);
			if (organization.isPresent()) {
				if (organization.get().isOrganisation()) {
					Optional<Organization> fhirOrganization = getTransformer().getFhirObject(organization.get());
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
					Optional<Organization> fhirOrganization = getTransformer().getFhirObject(organization);
					fhirOrganization.ifPresent(fp -> ret.add(fp));
				}
				return ret;
			}
		}
		return Collections.emptyList();
	}
}
