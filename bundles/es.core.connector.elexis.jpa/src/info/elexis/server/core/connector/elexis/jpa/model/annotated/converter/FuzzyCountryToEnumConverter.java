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

import info.elexis.server.core.connector.elexis.jpa.model.annotated.types.ISO3166_ALPHA_2_CountryCode;

/**
 * This converter allows "fuzziness" within the country value of an existing
 * database. The values should be set to {@link ISO3166_ALPHA_2_CountryCode} but
 * it can't be guaranteed, so in case a value not equal to the defined set is
 * observed it simply returns null instead of an Exception.
 */
public class FuzzyCountryToEnumConverter implements Converter {

	private static final long serialVersionUID = 439835332745734218L;

	@Override
	public String convertObjectValueToDataValue(Object objectValue, Session session) {
		if (objectValue == null) {
			return "";
		}
		ISO3166_ALPHA_2_CountryCode c = (ISO3166_ALPHA_2_CountryCode) objectValue;
		return c.name();
	}

	@Override
	public ISO3166_ALPHA_2_CountryCode convertDataValueToObjectValue(Object dataValue, Session session) {
		String in = (String) dataValue;
		ISO3166_ALPHA_2_CountryCode ret = null;
		try {
			ret = ISO3166_ALPHA_2_CountryCode.valueOf(in);
		} catch (IllegalArgumentException e) {
			ret = ISO3166_ALPHA_2_CountryCode.NDF;
		} catch (NullPointerException e) {
			ret = ISO3166_ALPHA_2_CountryCode.NDF;
		}
		return ret;
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public void initialize(DatabaseMapping mapping, Session session) {
	}

}
