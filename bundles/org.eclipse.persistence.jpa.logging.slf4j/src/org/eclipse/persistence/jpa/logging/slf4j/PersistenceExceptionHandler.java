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
package org.eclipse.persistence.jpa.logging.slf4j;

import org.eclipse.persistence.exceptions.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PersistenceExceptionHandler implements ExceptionHandler {

	private Logger log = LoggerFactory.getLogger(PersistenceExceptionHandler.class);

	@Override
	public Object handleException(RuntimeException exception) {
		log.error("PersistenceException: ", exception);
		// we have to throw the exception further, as EclipseLink
		// might be capable of handling it, e.g. by creating the required tables
		throw exception;
	}

}
