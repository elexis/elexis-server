package es.fhir.rest.core.transformer.helper;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Practitioner.PractitionerPractitionerRoleComponent;
import org.hl7.fhir.dstu3.model.valuesets.PractitionerRole;

public class MandantHelper extends AbstractHelper {

	public PractitionerPractitionerRoleComponent getPractitionerRoleComponent(String roleId) {
		PractitionerPractitionerRoleComponent component = new PractitionerPractitionerRoleComponent();
		CodeableConcept code = new CodeableConcept();
		if ("assistant".equals(roleId)) {
			code.addCoding(
					new Coding(PractitionerRole.NURSE.getSystem(), PractitionerRole.NURSE.toCode(),
							PractitionerRole.NURSE.toCode()));
		} else if ("doctor".equals(roleId)) {
			code.addCoding(new Coding(PractitionerRole.DOCTOR.getSystem(), PractitionerRole.DOCTOR.toCode(),
					PractitionerRole.DOCTOR.toCode()));
		} else if ("executive_doctor".equals(roleId)) {
			code.addCoding(new Coding("www.elexis.info/practRole", "mandant", "mandant"));
		} else {
			code.addCoding(new Coding("www.elexis.info/practRole", roleId, roleId));
		}
		component.setRole(code);
		return component;
	}

}
