package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import info.elexis.server.fhir.rest.core.test.AllTests;
import info.elexis.server.fhir.rest.core.test.FhirUtil;

public class TaskResourceTest {

	private static IGenericClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SQLException {
		AllTests.getTestDatabaseInitializer().initializeReminders();

		client = FhirUtil.getGenericClient("http://localhost:8380/fhir");
		assertNotNull(client);
	}

	@Test
	public void testSearch() {
		IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
		List<IReminder> allReminders = query.execute();

		Bundle results = client.search().forResource(Task.class)
				.where(Task.STATUS.exactly().code(TaskStatus.ACCEPTED.name())).returnBundle(Bundle.class).execute();
		assertEquals(allReminders.size(), results.getEntry().size());
		allReminders.get(0).setStatus(ProcessStatus.CLOSED);
		CoreModelServiceHolder.get().save(allReminders.get(0));
		results = client.search().forResource(Task.class)
				.where(Task.STATUS.exactly().code(TaskStatus.ACCEPTED.name())).returnBundle(Bundle.class).execute();
		assertEquals(allReminders.size() - 1, results.getEntry().size());

		results = client.search().forResource(Task.class).where(Task.STATUS.exactly().code(TaskStatus.COMPLETED.name()))
				.and(Task.OWNER.hasId(allReminders.get(0).getResponsible().get(0).getId())).returnBundle(Bundle.class)
				.execute();
		assertEquals(1, results.getEntry().size());
		results = client.search().forResource(Task.class).where(Task.STATUS.exactly().code(TaskStatus.COMPLETED.name()))
				.and(Task.OWNER.hasId("abcdef")).returnBundle(Bundle.class).execute();
		assertEquals(0, results.getEntry().size());

		results = client.search().forResource(Task.class).where(Task.STATUS.exactly().code(TaskStatus.COMPLETED.name()))
				.and(Task.PATIENT.hasId(allReminders.get(0).getContact().getId())).returnBundle(Bundle.class)
				.execute();
		assertEquals(1, results.getEntry().size());
		results = client.search().forResource(Task.class).where(Task.STATUS.exactly().code(TaskStatus.COMPLETED.name()))
				.and(Task.PATIENT.hasId("abcdef")).returnBundle(Bundle.class).execute();
		assertEquals(0, results.getEntry().size());
	}
}
