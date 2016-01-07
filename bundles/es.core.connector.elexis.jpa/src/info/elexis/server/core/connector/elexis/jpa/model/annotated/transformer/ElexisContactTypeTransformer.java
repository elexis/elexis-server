/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer;

import org.eclipse.persistence.mappings.foundation.AbstractTransformationMapping;
import org.eclipse.persistence.mappings.transformers.AttributeTransformer;
import org.eclipse.persistence.mappings.transformers.FieldTransformer;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.ContactType;

public class ElexisContactTypeTransformer implements AttributeTransformer, FieldTransformer {

	private static final long serialVersionUID = 1L;

	private AbstractTransformationMapping mapping;

	@Override
	public void initialize(AbstractTransformationMapping mapping) {
		this.mapping = mapping;
	}

	@Override
	public ContactType buildAttributeValue(Record row, Object object, Session session) {
		String istPerson = (String) row.get("KONTAKT.ISTPERSON");
		String istOrganisation = (String) row.get("KONTAKT.ISTORGANISATION");
		boolean person = false;
		boolean organisation = false;

		if (istPerson.trim().equals("1"))
			person = true;
		if (istOrganisation.trim().equals("1"))
			organisation = true;
		if (person && !organisation)
			return ContactType.PERSON;
		if (!person && organisation)
			return ContactType.ORGANIZATION;
		return ContactType.UNKNOWN;
	}

	@Override
	public String buildFieldValue(Object instance, String fieldName, Session session) {
//		Kontakt k = (Kontakt) instance;
//		switch (k.getContactType()) {
//		case PERSON:
//			if (fieldName.equals("istPerson"))
//				return "1";
//		case ORGANIZATION:
//			if (fieldName.equals("istOrganisation"))
//				return "1";
//		default:
//			return "0";
//		}
		return "";
	}
}
