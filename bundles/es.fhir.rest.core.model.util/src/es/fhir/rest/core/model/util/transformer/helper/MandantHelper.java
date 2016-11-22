package es.fhir.rest.core.model.util.transformer.helper;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Practitioner.PractitionerRoleComponent;

import ca.uhn.fhir.model.dstu.valueset.PractitionerRoleEnum;
import ch.elexis.core.findings.codes.CodingSystem;

public class MandantHelper extends AbstractHelper {

	public PractitionerRoleComponent getPractitionerRoleComponent(String roleId) {
		PractitionerRoleComponent component = new PractitionerRoleComponent();
		CodeableConcept code = new CodeableConcept();
		if ("assistant".equals(roleId)) {
			code.addCoding(
					new Coding(PractitionerRoleEnum.NURSE.getSystem(), PractitionerRoleEnum.NURSE.getCode(),
							PractitionerRoleEnum.NURSE.getCode()));
		} else if ("doctor".equals(roleId)) {
			code.addCoding(new Coding(PractitionerRoleEnum.DOCTOR.getSystem(), PractitionerRoleEnum.DOCTOR.getCode(),
					PractitionerRoleEnum.DOCTOR.getCode()));
		} else if ("executive_doctor".equals(roleId)) {
			code.addCoding(new Coding(CodingSystem.ELEXIS_PRACTITIONER_ROLE.getSystem(), "mandant", "mandant"));
		} else {
			code.addCoding(new Coding(CodingSystem.ELEXIS_PRACTITIONER_ROLE.getSystem(), roleId, roleId));
		}
		component.setCode(code);
		return component;
	}

}
