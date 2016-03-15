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

import java.time.LocalDateTime;
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

public class ElexisDBStringDateTimeTransformer implements AttributeTransformer, FieldTransformer {

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(ElexisDBStringDateTimeTransformer.class);

	private AbstractTransformationMapping mapping;
	
	private final DateTimeFormatter yyyyMMddHHmmss = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	@Override
	public void initialize(AbstractTransformationMapping mapping) {
		this.mapping = mapping;
	}

	@Override
	public LocalDateTime buildAttributeValue(Record row, Object object, Session session) {
		String dateString = (String) row.get(mapping.getAttributeName());
		// TODO unset has to be represented with null, that is ""
		if (dateString == null || dateString.length() == 0)
			return null;
		try {
			return LocalDateTime.parse(dateString, yyyyMMddHHmmss);
		} catch (DateTimeParseException e) {
			log.warn("Error parsing {} in {}", dateString, ((AbstractDBObjectIdDeleted) object).getId(), e);
		}
		return null;
	}

	@Override
	public String buildFieldValue(Object instance, String fieldName, Session session) {
		Object obj = mapping.getAttributeValueFromObject(instance);
		if (obj == null)
			return null;
		LocalDateTime date = (LocalDateTime) obj;
		return date.format(yyyyMMddHHmmss);
	}

}
