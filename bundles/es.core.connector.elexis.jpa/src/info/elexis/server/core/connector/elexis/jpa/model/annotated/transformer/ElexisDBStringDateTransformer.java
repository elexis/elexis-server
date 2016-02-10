/*******************************************************************************
 * Copyright (c) 2016 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package info.elexis.server.core.connector.elexis.jpa.model.annotated.transformer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.eclipse.persistence.mappings.foundation.AbstractTransformationMapping;
import org.eclipse.persistence.mappings.transformers.AttributeTransformer;
import org.eclipse.persistence.mappings.transformers.FieldTransformer;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;

public class ElexisDBStringDateTransformer implements AttributeTransformer, FieldTransformer {

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(ElexisDBStringDateTransformer.class);

	private AbstractTransformationMapping mapping;
	
	private final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Override
	public void initialize(AbstractTransformationMapping mapping) {
		this.mapping = mapping;
	}

	@Override
	public LocalDate buildAttributeValue(Record row, Object object, Session session) {
		LocalDate dob = null;
		String dateString = (String) row.get(mapping.getAttributeName());
		// TODO unset has to be represented with null, that is ""
		if (dateString == null || dateString.length() == 0)
			return dob;
		try {
			dob = LocalDate.parse(dateString, yyyyMMdd);
		} catch (DateTimeParseException e) {
			log.warn("Error parsing {} in {}", dateString, ((AbstractDBObjectIdDeleted) object).getId(), e);
		}
		return dob;
	}

	@Override
	public String buildFieldValue(Object instance, String fieldName, Session session) {
		Object obj = mapping.getAttributeValueFromObject(instance);
		if (obj == null)
			return null;
		LocalDate date = (LocalDate) obj;
		return date.format(yyyyMMdd);
	}

}
