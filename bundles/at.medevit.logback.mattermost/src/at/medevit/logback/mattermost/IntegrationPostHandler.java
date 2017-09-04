package at.medevit.logback.mattermost;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

public class IntegrationPostHandler {

	private final ILoggingEvent eventObject;
	private final String identification;

	public IntegrationPostHandler(ILoggingEvent eventObject, String identification) {
		this.eventObject = eventObject;
		this.identification = identification;
	}

	public int post(String url) throws MalformedURLException, IOException {
		Level logLevel = eventObject.getLevel();
		if (logLevel == null) {
			logLevel = Level.INFO;
		}

		JSONObject json = new JSONObject();

		if (identification != null) {
			json.put("username", identification);
		}

		ZonedDateTime eventTimeStamp = Instant.ofEpochMilli(eventObject.getTimeStamp()).atZone(ZoneId.of("GMT+1"));
		StringBuilder sbHeader = new StringBuilder();
		sbHeader.append(eventTimeStamp.toLocalDateTime() + " @" + logLevel.levelStr);
		sbHeader.append(" _" + eventObject.getLoggerName() + "_\n");
		json.put("text", sbHeader.toString());

		Map<String, Object> params = new HashMap<>();
		params.put("color", levelToColor(logLevel));
		params.put("title", eventObject.getFormattedMessage());

		if (eventObject.getThrowableProxy() != null) {
			StringBuilder sbException = new StringBuilder();
			IThrowableProxy throwableProxy = eventObject.getThrowableProxy();

			sbException.append("```\n");
			int i = 0;
			StackTraceElementProxy[] st = throwableProxy.getStackTraceElementProxyArray();
			for (StackTraceElementProxy stpe : st) {
				i++;
				sbException.append(stpe + "\n");
				if (i == 5) {
					break;
				}
			}
			sbException.append("```");
			params.put("text", sbException.toString());
		}

		json.put("attachments", Collections.singletonList(params));

		return send(json.toString().getBytes(), url);

	}

	private Object levelToColor(Level logLevel) {
		switch (logLevel.levelInt) {
		case Level.ERROR_INT:
			return "#FF0000";
		case Level.WARN_INT:
			return "#FFDB00";
		default:
			return "#0000FF";
		}
	}

	private int send(byte[] postDataBytes, String url) throws MalformedURLException, IOException {
		HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		con.getOutputStream().write(postDataBytes);

		return con.getResponseCode();
	}
}
