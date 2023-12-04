package info.elexis.server.core.redmine.internal.jaxrs;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineSecurityException;

import info.elexis.jaxrs.service.JaxrsResource;
import info.elexis.server.core.redmine.internal.RedmineUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Path("redmine")
@Component
public class RedmineResource implements JaxrsResource {

	@POST
	@Path("sendlogfile")
	@Operation(summary = "send log file to redmine")
	public Response sendLogFileToRedmine(
			@Parameter(required = false, description = "sendlog configuration") SendLogConfiguration sendConfiguration) {

		String appenderName = null;
		Integer issueId = null;
		Long sizeLimit = null;

		if (sendConfiguration != null) {
			appenderName = sendConfiguration.getAppender();
			issueId = sendConfiguration.getIssue();
			sizeLimit = sendConfiguration.getMaxsize();
		}

		try {
			String issueUrl = RedmineUtil.INSTANCE.sendLogToRedmine(appenderName, issueId, sizeLimit);
			return Response.ok(issueUrl, MediaType.TEXT_PLAIN_TYPE).build();
		} catch (RedmineException | IOException e) {
			LoggerFactory.getLogger(getClass()).warn("Error sending log", e);
			if (e instanceof RedmineSecurityException) {
				return Response.status(Status.UNAUTHORIZED).entity(e.getMessage()).build();
			}
			return Response.serverError().build();
		}

	}

}
