package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Practitioner.PractitionerRoleComponent;
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

	@Search()
	public List<Practitioner> findPractitioner(@RequiredParam(name = Practitioner.SP_ROLE) CodeType roleCode) {
		if (roleCode != null) {
			String codeValue = roleCode.getValue();
			if (codeValue != null) {
				String[] parts = codeValue.split("\\|");
				if (parts.length == 2) {
					String codeSystem = parts[0];
					String codeCode = parts[1];
					List<Practitioner> allPractitioners = getAllPractitioners();
					return allPractitioners.stream()
							.filter(p -> practitionerHasRole(p, codeSystem, codeCode))
							.collect(Collectors.toList());

				}
			}
		}
		return Collections.emptyList();
	}

	private boolean practitionerHasRole(Practitioner p, String codeSystem, String codeCode) {
		List<PractitionerRoleComponent> roles = p.getRole();
		for (PractitionerRoleComponent practitionerRoleComponent : roles) {
			List<Coding> codings = practitionerRoleComponent.getCode().getCoding();
			for (Coding coding : codings) {
				if (coding.getSystem().equals(codeSystem) && coding.getCode().equals(codeCode)) {
					return true;
				}
			}
		}
		return false;
	}

	private List<Practitioner> getAllPractitioners() {
		JPAQuery<Kontakt> query = new JPAQuery<>(Kontakt.class);
		query.add(Kontakt_.mandator, QUERY.EQUALS, true);
		List<Kontakt> practitioners = query.execute();
		List<Practitioner> ret = new ArrayList<Practitioner>();
		if (!practitioners.isEmpty()) {
			for (Kontakt practitioner : practitioners) {
				Optional<Practitioner> fhirPractitioner = practitionerMapper.getFhirObject(practitioner);
				fhirPractitioner.ifPresent(fp -> ret.add(fp));
			}
		}
		return ret;
	}
}