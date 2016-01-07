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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.persistence.mappings.foundation.AbstractTransformationMapping;
import org.eclipse.persistence.mappings.transformers.AttributeTransformer;
import org.eclipse.persistence.mappings.transformers.FieldTransformer;
import org.eclipse.persistence.sessions.Record;
import org.eclipse.persistence.sessions.Session;

public class ElexisDBStringDateTransformer implements AttributeTransformer, FieldTransformer {
	
	private static final long serialVersionUID = 1L;
	
	private AbstractTransformationMapping mapping;
	SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyyMMdd");
	
	@Override
	public void initialize(AbstractTransformationMapping mapping){
		this.mapping = mapping;
	}
	
	@Override
	public Date buildAttributeValue(Record row, Object object, Session session){
		Date dob = null;	
		String dateString = (String) row.get(mapping.getAttributeName());
		// TODO unset has to be represented with null, that is ""
		if(dateString == null) return dob;
		try {
			dob = yyyyMMdd.parse(dateString);
		} catch (ParseException e) {
			System.out.println(e);
		}
		return dob;
	}
	
	@Override
	public String buildFieldValue(Object instance, String fieldName, Session session){
		Object obj = mapping.getAttributeValueFromObject(instance);
		if (obj == null) return null;
		Date date = (Date) obj;
		return yyyyMMdd.format(date);
	}
	
}
