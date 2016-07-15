/*******************************************************************************
 * Copyright (c) 2016, MEDEVIT
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    MEDEVIT <office@medevit.at>
 *******************************************************************************/
package at.medevit.logback.pushnotification;

import java.util.ArrayList;
import java.util.List;

import com.usk.lib.NMAClientLib;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import net.sourceforge.prowl.api.DefaultProwlEvent;
import net.sourceforge.prowl.api.ProwlClient;
import net.sourceforge.prowl.api.ProwlEvent;
import net.sourceforge.prowl.exception.ProwlException;

public class PushNotificationAppender extends AppenderBase<ILoggingEvent> {

	private String application;
	private String apiKeys;
	private String event;

	private String joinedProwlApiKeys;
	private String joinedNmaApiKeys;

	@Override
	public void start() {
		if (this.apiKeys == null) {
			addError("No api key defined, please set the <apiKey> parameter.");
			return;
		}

		if (application == null) {
			application = "logback";
			addInfo("No <application> parameter defined, defaulting to logback.");
		}

		initializeApiKeyStrings();

		if (joinedNmaApiKeys == null && joinedProwlApiKeys == null) {
			addError("No valid apiKeys defined.");
			return;
		}

		super.start();
	}

	@Override
	protected void append(ILoggingEvent eventObject) {
		if (event == null) {
			event = "Log " + eventObject.getLevel().levelStr;
		}

		Level level = eventObject.getLevel();
		int priority = determinePriorityByLogLevel(level);

		StringBuilder msgBuilder = new StringBuilder();
		msgBuilder.append(eventObject.getFormattedMessage());
		IThrowableProxy throwableProxy = eventObject.getThrowableProxy();
		if (throwableProxy != null) {
			msgBuilder.append(" " + throwableProxy.getMessage());
		}

		final String message = msgBuilder.toString();

		if (joinedProwlApiKeys != null) {
			ProwlEvent prowlEvent = new DefaultProwlEvent(joinedProwlApiKeys, application, event, message, priority);
			try {
				String result = new ProwlClient(3000, 3000).pushEvent(prowlEvent);
				addInfo(result);
			} catch (ProwlException e) {
				addError("Error pushing prowl event", e);
			}
		}

		if (joinedNmaApiKeys != null) {
			if (NMAClientLib.notify(application, event, message, priority, joinedNmaApiKeys) == 1) {
				addInfo("NMAClientLib: Message sent!");
			} else {
				addError("Error pushing NMA event: " + NMAClientLib.getLastError());
			}
		}
	}

	private void initializeApiKeyStrings() {
		List<String> prowlApiKeys = new ArrayList<String>();
		List<String> nmaApiKeys = new ArrayList<String>();

		// PROWL (https://www.prowlapp.com/api.php) Each API key is a 40-byte
		// hexadecimal string.
		// NMA (https://www.notifymyandroid.com/api.jsp) Each API key is a 48
		// bytes hexadecimal string.
		String[] keys = apiKeys.split(",");
		for (String key : keys) {
			if (key.length() == 40) {
				prowlApiKeys.add(key);
			} else if (key.length() == 48) {
				nmaApiKeys.add(key);
			} else {
				addError("Invalid key length (" + key.length() + ") " + key);
			}
		}

		if (prowlApiKeys.size() > 0) {
			joinedProwlApiKeys = (String) prowlApiKeys.stream().map(o -> o.toString()).reduce((u, t) -> u + "," + t)
					.get();
		}

		if (nmaApiKeys.size() > 0) {
			joinedNmaApiKeys = (String) nmaApiKeys.stream().map(o -> o.toString()).reduce((u, t) -> u + "," + t).get();
		}
	}

	private int determinePriorityByLogLevel(Level level) {
		switch (level.levelInt) {
		case Level.ERROR_INT:
			return 2;
		case Level.WARN_INT:
			return 1;
		case Level.INFO_INT:
			return 0;
		case Level.DEBUG_INT:
			return -1;
		case Level.TRACE_INT:
			return -2;
		default:
			break;
		}
		return 0;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getApiKeys() {
		return apiKeys;
	}

	public void setApiKeys(String apiKeys) {
		this.apiKeys = apiKeys;
	}

}
