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

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;

@Component
public class OrganizationResourceProvider implements IFhirResourceProvider {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Organization.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<Organization, IOrganization> getTransformer() {
		return (IFhirTransformer<Organization, IOrganization>) transformerRegistry.getTransformerFor(Organization.class,
				IOrganization.class);
	}

	@Read
	public Organization getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IOrganization> organization = modelService.load(idPart, IOrganization.class);
			if (organization.isPresent()) {
				Optional<Organization> fhirOrganization = getTransformer().getFhirObject(organization.get());
				return fhirOrganization.get();

			}
		}
		return null;
	}

	@Search()
	public List<Organization> findOrganization(@RequiredParam(name = Organization.SP_NAME) String name) {
		if (name != null) {
			IQuery<IOrganization> query = modelService.getQuery(IOrganization.class);
			query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "%" + name + "%", true);
			query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE, "%" + name + "%", true);
			List<IOrganization> organizations = query.execute();
			if (!organizations.isEmpty()) {
				List<Organization> ret = new ArrayList<>();
				for (IOrganization organization : organizations) {
					Optional<Organization> fhirOrganization = getTransformer().getFhirObject(organization);
					fhirOrganization.ifPresent(ret::add);
				}
				return ret;
			}
		}
		return Collections.emptyList();
	}
}
