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
package info.elexis.server.core.connector.elexis.jpa.model.annotated.converter;

import org.eclipse.persistence.mappings.DatabaseMapping;
import org.eclipse.persistence.mappings.converters.Converter;
import org.eclipse.persistence.sessions.Session;

import ch.rgw.compress.CompEx;
import ch.rgw.tools.StringTool;

/**
 * Converts a string into a compressed array, similar to the mapping notation S:C: used in
 * PersistentObject. The method is identical.
 * 
 * @author M. Descher, MEDEVIT, Austria
 */
public class ElexisDBCompressedStringConverter implements Converter {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public byte[] convertObjectValueToDataValue(Object objectValue, Session session){
		if (objectValue == null)
			return new byte[0];
		return CompEx.Compress((String) objectValue, CompEx.ZIP);
	}
	
	@Override
	public String convertDataValueToObjectValue(Object dataValue, Session session){
		if (dataValue == null)
			return "";
		try {
			byte[] exp = CompEx.expand((byte[]) dataValue);
			return StringTool.createString(exp);
		} catch (Exception e) {
			e.printStackTrace();
			// If we face an error during un-compression we simply return an empty string
			// this should be better fixed in CompEx.expand
			return "";
		}
	}
	
	@Override
	public boolean isMutable(){
		return false;
	}
	
	@Override
	public void initialize(DatabaseMapping mapping, Session session){}
	
}
