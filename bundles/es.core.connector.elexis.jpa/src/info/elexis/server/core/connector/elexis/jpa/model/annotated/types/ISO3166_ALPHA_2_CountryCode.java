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
package info.elexis.server.core.connector.elexis.jpa.model.annotated.types;

/**
 * Enumeration of ISO 3166 ALPHA 2 country codes. This list is not complete, only
 * the values required so far are provided. Please see {@link http://en.wikipedia.org/wiki/ISO_3166-1}
 * for a complete list.<br>
 * The values are used for unambiguous identification of country codes within Elexis
 */
public enum ISO3166_ALPHA_2_CountryCode {
	NDF(000), // Not, or incorrectly defined
	AT(040), CH(756), DE(276), FR(250), LI(438);
	
	/** The numeric country code */
	private final Integer value;
	
	private ISO3166_ALPHA_2_CountryCode(Integer value){
		this.value = value;
	}
	
	public Integer getValue(){
		return value;
	}
}
