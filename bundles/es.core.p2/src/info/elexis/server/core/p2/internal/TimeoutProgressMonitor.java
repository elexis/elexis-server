/*******************************************************************************
 * Copyright (c) 2015 Bruno Medeiros and other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bruno Medeiros - initial API and implementation
 *******************************************************************************/
package info.elexis.server.core.p2.internal;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Bruno Medeiros
 * @see https://github.com/am2605/CfmlEclipseIDE
 */
public class TimeoutProgressMonitor implements IProgressMonitor {
	
	protected int timeoutMillis;
	protected long startTimeMillis = -1;
	
	public TimeoutProgressMonitor(int timeoutMillis) {
		this.timeoutMillis = timeoutMillis;
		this.startTimeMillis = System.currentTimeMillis();
	}
	
	public int getTimeoutMillis() {
		return timeoutMillis;
	}
	
	@Override
	public void beginTask(String name, int totalWork) {
	}
	
	@Override
	public boolean isCanceled() {
		return System.currentTimeMillis() - startTimeMillis > timeoutMillis;
	}
	
	@Override
	public void done() {
	}
	
	@Override
	public void internalWorked(double work) {
	}
	
	@Override
	public void setCanceled(boolean value) {
		if(value) {
			timeoutMillis = 0;
		}
	}
	
	@Override
	public void setTaskName(String name) {
	}
	
	@Override
	public void subTask(String name) {
	}
	
	@Override
	public void worked(int work) {
	}
	
}