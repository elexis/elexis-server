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
package info.elexis.server.core.connector.elexis.jpa.datasource;

import java.util.Vector;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.internal.databaseaccess.Accessor;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.sequencing.Sequence;
import org.eclipse.persistence.sessions.DatabaseLogin;
import org.eclipse.persistence.sessions.JNDIConnector;
import org.eclipse.persistence.sessions.Session;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.id.ElexisIdGenerator;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.id.UUIDSequence;

public class ElexisSessionCustomizer extends Sequence implements SessionCustomizer {

	private static final long serialVersionUID = 1L;

	public ElexisSessionCustomizer() {
		super();
	}

	public ElexisSessionCustomizer(String name) {
		super(name);
	}

	@Override
	public Object getGeneratedValue(Accessor accessor, AbstractSession writeSession, String seqName) {
		return ElexisIdGenerator.generateId();
	}

	@Override
	public Vector<?> getGeneratedVector(Accessor accessor, AbstractSession writeSession, String seqName, int size) {
		return null;
	}

	@Override
	public void onConnect() {
	}

	@Override
	public void onDisconnect() {
	}

	@Override
	public boolean shouldAcquireValueAfterInsert() {
		return false;
	}

	public boolean shouldOverrideExistingValue(String seqName, Object existingValue) {
		return ((String) existingValue).isEmpty();
	}

	@Override
	public boolean shouldUseTransaction() {
		return false;
	}

	@Override
	public boolean shouldUsePreallocation() {
		return false;
	}

	public void customize(Session session) throws Exception {
		DatabaseLogin databaseLogin = session.getLogin();
		if (databaseLogin != null) {
			// set datasource to poolable datasource
			JNDIConnector conn = (JNDIConnector) databaseLogin.getConnector();
			ElexisDataSourceUtil.getDataSource(session)
				.ifPresent(dataSource -> conn.setDataSource(dataSource));
			
			// add sequence for system uuid
			UUIDSequence sequence = new UUIDSequence("system-uuid");
			databaseLogin.addSequence(sequence);
		}
		
	}

}
