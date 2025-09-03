package es.fhir.rest.core.resources;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Person;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import es.fhir.rest.core.resources.util.IContactSearchFilterQueryAdapter;

@Component(service = PlainResourceProvider.class, immediate = true)
public class PlainResourceProvider {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	protected IModelService coreModelService;

	@Reference
	private IContextService contextService;

	@Reference
	private IFhirTransformerRegistry transformerRegistry;

	@Search
	public Bundle search(@RequiredParam(name = Constants.PARAM_TYPE) StringAndListParam theType,
			@OptionalParam(name = ca.uhn.fhir.rest.api.Constants.PARAM_FILTER) StringAndListParam theFtFilter) {

		Bundle bundle = new Bundle();

		List<StringOrListParam> type_values = theType.getValuesAsQueryTokens();
		List<String> types = type_values.get(0).getValuesAsQueryTokens().stream().map(StringParam::getValue).toList();

		boolean searchPerson = types.contains(Person.class.getSimpleName());
		boolean searchPatient = types.contains(Patient.class.getSimpleName());
		boolean searchOrganization = types.contains(Organization.class.getSimpleName());
		if (searchPerson || searchPatient || searchOrganization) {

			IQuery<IContact> query = coreModelService.getQuery(IContact.class);
			query.startGroup();
			if (searchPerson) {
				query.or(ModelPackage.Literals.ICONTACT__PERSON, COMPARATOR.EQUALS, searchPerson);
			}
			if (searchPatient) {
				query.or(ModelPackage.Literals.ICONTACT__PATIENT, COMPARATOR.EQUALS, searchPatient);
			}
			if (searchOrganization) {
				query.or(ModelPackage.Literals.ICONTACT__ORGANIZATION, COMPARATOR.EQUALS, searchOrganization);
			}
			query.andJoinGroups();

			if (theFtFilter != null) {
				new IContactSearchFilterQueryAdapter().adapt(query, theFtFilter);
			}

			List<IContact> contacts = query.execute();
			List<BundleEntryComponent> entryComponents = contextService
					.submitContextInheriting(() -> contacts.parallelStream().map(this::mapByType)
							.filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()));
			entryComponents.forEach(bundle::addEntry);
		}

		bundle.setTotal(bundle.getEntry().size());
		return bundle;
	}

	private Optional<BundleEntryComponent> mapByType(IContact contact) {
		BundleEntryComponent bundleEntryComponent = new BundleEntryComponent();
		if (contact.isPatient()) {
			IFhirTransformer<Patient, IPatient> transformer = transformerRegistry.getTransformerFor(Patient.class,
					IPatient.class);
			bundleEntryComponent.setResource(transformer.getFhirObject(contact.asIPatient()).get());
			return Optional.of(bundleEntryComponent);
		}
		if (contact.isPerson()) {
			IFhirTransformer<Person, IPerson> transformer = transformerRegistry.getTransformerFor(Person.class,
					IPerson.class);
			bundleEntryComponent.setResource(transformer.getFhirObject(contact.asIPerson()).get());
			return Optional.of(bundleEntryComponent);
		}

		IFhirTransformer<Organization, IOrganization> transformer = transformerRegistry
				.getTransformerFor(Organization.class, IOrganization.class);
		bundleEntryComponent.setResource(transformer.getFhirObject(contact.asIOrganization()).get());
		return Optional.of(bundleEntryComponent);
	}

}
