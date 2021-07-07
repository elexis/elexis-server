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

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import es.fhir.rest.core.IFhirResourceProvider;
import es.fhir.rest.core.IFhirTransformer;
import es.fhir.rest.core.IFhirTransformerRegistry;
import es.fhir.rest.core.resources.util.CodeTypeUtil;

@Component
public class PractitionerRoleResourceProvider implements IFhirResourceProvider {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference
	private IFhirTransformerRegistry transformerRegistry;
	
	@Override
	public Class<? extends IBaseResource> getResourceType(){
		return PractitionerRole.class;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public IFhirTransformer<PractitionerRole, IUser> getTransformer(){
		return (IFhirTransformer<PractitionerRole, IUser>) transformerRegistry
			.getTransformerFor(PractitionerRole.class, IUser.class);
	}
	
	@Read
	public PractitionerRole getResourceById(@IdParam
	IdType theId){
		String idPart = theId.getIdPart();
		if (idPart != null) {
			Optional<IUser> user = modelService.load(idPart, IUser.class);
			if (user.isPresent()) {
				Optional<PractitionerRole> fhirPractitionerRole =
					getTransformer().getFhirObject(user.get());
				return fhirPractitionerRole.get();
			}
		}
		return null;
	}
	
	@Search()
	public List<PractitionerRole> findPractitionerRole(
		@RequiredParam(name = PractitionerRole.SP_ROLE)
		CodeType roleCode){
		if (roleCode != null) {
			Optional<String> codeSystem = CodeTypeUtil.getSystem(roleCode);
			Optional<String> codeCode = CodeTypeUtil.getCode(roleCode);
			List<PractitionerRole> allPractitionerRoles = getAllPractitionerRoles();
			return allPractitionerRoles.stream()
				.filter(pr -> practitionerRoleHasCode(pr, codeSystem, codeCode))
				.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
	
	private boolean practitionerRoleHasCode(PractitionerRole pr, Optional<String> codeSystem,
		Optional<String> codeCode){
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
	
	private List<PractitionerRole> getAllPractitionerRoles(){
		// all Kontakt marked as user
		IQuery<IUser> query = modelService.getQuery(IUser.class);
		List<IUser> practitioners = query.execute();
		List<PractitionerRole> ret = new ArrayList<PractitionerRole>();
		if (!practitioners.isEmpty()) {
			for (IUser user : practitioners) {
				Optional<PractitionerRole> fhirPractitionerRole =
					getTransformer().getFhirObject(user);
				fhirPractitionerRole.ifPresent(fp -> ret.add(fp));
			}
		}
		return ret;
	}
}
