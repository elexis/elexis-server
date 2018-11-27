package info.elexis.server.webapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.stream.Collectors;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Platform;
import org.osgi.service.component.annotations.Component;

@Component(service = Servlet.class, property = {
		"osgi.http.whiteboard.servlet.pattern=/web/elexis-connector/connection" })
public class ConnectorDatabaseConfigurationWebappIndex extends HttpServlet {

	private static final long serialVersionUID = 4821073364544828155L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		try (InputStream inputStream = Platform.getBundle("info.elexis.server.webapp")
				.getResource("/web/elexis-connector/connection/index.html").openStream()) {

			String response = new BufferedReader(new InputStreamReader(inputStream)).lines()
					.collect(Collectors.joining("\n"));

			PrintWriter writer = resp.getWriter();
			writer.write(response);
		}
	}
}
