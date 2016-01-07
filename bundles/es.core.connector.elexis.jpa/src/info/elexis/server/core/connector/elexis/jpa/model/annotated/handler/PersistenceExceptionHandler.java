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
package info.elexis.server.core.connector.elexis.jpa.model.annotated.handler;

import org.eclipse.persistence.exceptions.ExceptionHandler;

public class PersistenceExceptionHandler implements ExceptionHandler {
	
	@Override
	public Object handleException(RuntimeException exception){
		// TODO integrate into OSGI logger
		exception.printStackTrace();
		return null;
	}
	
}
