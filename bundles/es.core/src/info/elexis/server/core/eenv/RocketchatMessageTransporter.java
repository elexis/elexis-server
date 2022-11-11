package info.elexis.server.core.eenv;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import com.google.gson.Gson;

import ch.elexis.core.model.message.MessageCode;
import ch.elexis.core.model.message.TransientMessage;
import ch.elexis.core.services.IMessageTransporter;

public class RocketchatMessageTransporter implements IMessageTransporter {

	private final String rocketchatIntegrationUrl;

	public RocketchatMessageTransporter(String rocketchatIntegrationUrl) {
		this.rocketchatIntegrationUrl = rocketchatIntegrationUrl;
	}

	@Override
	public String getUriScheme() {
		return "rocketchat";
	}

	@Override
	public boolean isExternal() {
		return false;
	}

	@Override
	public IStatus send(TransientMessage message) {
		return sendFromStationSender(message);
	}

	private IStatus sendFromStationSender(TransientMessage message) {
		try {
			URL integrationUrl = new URL(rocketchatIntegrationUrl);

			String jsonMessage = prepareRocketchatMessage(message);
			return send(integrationUrl, jsonMessage.getBytes());

		} catch (IOException e) {
			return new Status(IStatus.ERROR, "info.elexis.server.core", e.getMessage());
		}
	}

	protected String prepareRocketchatMessage(TransientMessage message) {

		String severity = message.getMessageCodes().get(MessageCode.Key.Severity);
		if (severity == null) {
			severity = MessageCode.Value.Severity_INFO;
		}

		StringBuilder header = new StringBuilder();
		header.append(severityToEmoji(severity) + " @"
				+ message.getReceiver().substring(message.getReceiver().indexOf(':') + 1));

		Set<Entry<String, String>> entrySet = message.getMessageCodes().entrySet();
		if (!entrySet.isEmpty()) {
			header.append(" | ");
			message.getMessageCodes().entrySet()
					.forEach(c -> header.append(c.getKey() + ":" + c.getValue() + StringUtils.SPACE));
		}

		Map<String, Object> params = new HashMap<>();
		params.put("color", severityToColor(severity));
		params.put("text", message.getMessageText());

		RocketchatMessage rocketchatMessage = new RocketchatMessage();
		rocketchatMessage.setSender(message.getSender());
		rocketchatMessage.setText(header.toString());
		rocketchatMessage.setAttachments(params);
		return new Gson().toJson(rocketchatMessage);
	}

	private IStatus send(URL url, byte[] postDataBytes) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		con.setUseCaches(false);
		con.setRequestProperty("Content-Type", "application/json");
		con.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		con.getOutputStream().write(postDataBytes);

		int responseCode = con.getResponseCode();
		if (responseCode == 200) {
			return Status.OK_STATUS;
		}
		return new Status(IStatus.ERROR, "info.elexis.server.core",
				"Error sending, with response code: " + responseCode);
	}

	private String severityToColor(String severity) {
		switch (severity) {
		case MessageCode.Value.Severity_WARN:
			return "#FFDB00";
		case MessageCode.Value.Severity_ERROR:
			return "#FF0000";
		default:
			return "#0000FF";
		}
	}

	private String severityToEmoji(String severity) {
		switch (severity) {
		case MessageCode.Value.Severity_ERROR:
			return ":stop_sign:";
		case MessageCode.Value.Severity_WARN:
			return ":warning:";
		default:
			return StringUtils.EMPTY;
		}
	}

}
