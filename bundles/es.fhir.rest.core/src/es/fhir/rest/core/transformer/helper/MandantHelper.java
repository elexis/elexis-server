package es.fhir.rest.core.transformer.helper;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Practitioner.PractitionerPractitionerRoleComponent;

import ca.uhn.fhir.model.dstu.valueset.PractitionerRoleEnum;

public class MandantHelper extends AbstractHelper {

	public PractitionerPractitionerRoleComponent getPractitionerRoleComponent(String roleId) {
		PractitionerPractitionerRoleComponent component = new PractitionerPractitionerRoleComponent();
		CodeableConcept code = new CodeableConcept();
		if ("assistant".equals(roleId)) {
			code.addCoding(
					new Coding(PractitionerRoleEnum.VALUESET_IDENTIFIER, PractitionerRoleEnum.NURSE.getCode(),
							PractitionerRoleEnum.NURSE.getCode()));
		} else if ("doctor".equals(roleId)) {
			code.addCoding(new Coding(PractitionerRoleEnum.VALUESET_IDENTIFIER, PractitionerRoleEnum.DOCTOR.getCode(),
					PractitionerRoleEnum.DOCTOR.getCode()));
		} else if ("executive_doctor".equals(roleId)) {
			code.addCoding(new Coding("www.elexis.info/practRole", "mandant", "mandant"));
		} else {
			code.addCoding(new Coding("www.elexis.info/practRole", roleId, roleId));
		}
		component.setRole(code);
		return component;
	}

}
