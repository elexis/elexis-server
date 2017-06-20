package es.fhir.rest.core.resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.PractitionerRole;
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
import es.fhir.rest.core.resources.util.CodeTypeUtil;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.User;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.UserService;

@Component
public class PractitionerRoleResourceProvider implements IFhirResourceProvider {

	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return PractitionerRole.class;
	}

	private IFhirTransformerRegistry transformerRegistry;

	@Reference(cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "-")
	protected void bindIFhirTransformerRegistry(IFhirTransformerRegistry transformerRegistry) {
		this.transformerRegistry = transformerRegistry;
	}

	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<PractitionerRole, User> getTransformer() {
		return (IFhirTransformer<PractitionerRole, User>) transformerRegistry.getTransformerFor(PractitionerRole.class,
				User.class);
	}

	@Read
	public PractitionerRole getResourceById(@IdParam IdType theId) {
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<User> user = UserService.load(idPart);
			if (user.isPresent()) {
				Optional<PractitionerRole> fhirPractitionerRole = getTransformer().getFhirObject(user.get());
				return fhirPractitionerRole.get();
			}
		}
		return null;
	}

	@Search()
	public List<PractitionerRole> findPractitionerRole(
			@RequiredParam(name = PractitionerRole.SP_ROLE) CodeType roleCode) {
		if (roleCode != null) {
			Optional<String> codeSystem = CodeTypeUtil.getSystem(roleCode);
			Optional<String> codeCode = CodeTypeUtil.getCode(roleCode);
			List<PractitionerRole> allPractitionerRoles = getAllPractitionerRoles();
			return allPractitionerRoles.stream().filter(pr -> practitionerRoleHasCode(pr, codeSystem, codeCode))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private boolean practitionerRoleHasCode(PractitionerRole pr, Optional<String> codeSystem,
			Optional<String> codeCode) {
		List<CodeableConcept> roles = pr.getCode();
		for (CodeableConcept code : roles) {
			List<Coding> codings = code.getCoding();
			for (Coding coding : codings) {
				boolean matchingSystem = false;
				boolean matchingCode = false;
				if (codeSystem.isPresent()) {
					matchingSystem = coding.getSystem().equals(codeSystem.get());
				}
				if (codeCode.isPresent()) {
					matchingCode = coding.getCode().equals(codeCode.get());
				}
				if (matchingSystem && matchingCode) {
					return true;
				}
			}
		}
		return false;
	}

	private List<PractitionerRole> getAllPractitionerRoles() {
		// all Kontakt marked as user
		JPAQuery<Kontakt> query = new JPAQuery<>(Kontakt.class);
		query.add(Kontakt_.user, QUERY.EQUALS, true);
		List<Kontakt> practitioners = query.execute();
		List<PractitionerRole> ret = new ArrayList<PractitionerRole>();
		if (!practitioners.isEmpty()) {
			for (Kontakt kontakt : practitioners) {
				Optional<User> user = UserService.findByKontakt(kontakt);
				user.ifPresent(u -> {
					Optional<PractitionerRole> fhirPractitionerRole = getTransformer().getFhirObject(u);
					fhirPractitionerRole.ifPresent(fp -> ret.add(fp));
				});
			}
		}
		return ret;
	}
}
