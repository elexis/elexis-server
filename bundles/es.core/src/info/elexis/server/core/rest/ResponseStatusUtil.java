package info.elexis.server.core.rest;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class ResponseStatusUtil {

	/**
	 * Convert an {@link IStatus} to a {@link Response}
	 * 
	 * @param status
	 * @return
	 */
	public static Response convert(IStatus status) {
		StringBuilder message = new StringBuilder();
		message.append(status.getMessage());

		Throwable exception = status.getException();
		if (exception != null) {
			message.append(" " + exception.getMessage() + "]");
		}

		Response.Status respStatus = Response.Status.OK;
		if (status.getSeverity() == Status.ERROR) {
			respStatus = Response.Status.INTERNAL_SERVER_ERROR;
		}

		return Response.status(respStatus).entity(message.toString()).type(MediaType.TEXT_PLAIN_TYPE).build();
	}

}
