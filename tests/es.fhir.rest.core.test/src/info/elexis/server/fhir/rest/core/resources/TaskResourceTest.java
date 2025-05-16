package info.elexis.server.fhir.rest.core.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Task;
import org.hl7.fhir.r4.model.Task.TaskIntent;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.builder.IReminderBuilder;
import ch.elexis.core.model.builder.IUserGroupBuilder;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
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

		results = client.search().byUrl("Task?status:not=COMPLETED").returnBundle(Bundle.class).execute();
		assertEquals(allReminders.size() - 1, results.getEntry().size());
		allReminders.get(1).setStatus(ProcessStatus.CLOSED);
		CoreModelServiceHolder.get().save(allReminders.get(1));
		results = client.search().byUrl("Task?status:not=COMPLETED").returnBundle(Bundle.class).execute();
		assertEquals(allReminders.size() - 2, results.getEntry().size());

		IUserGroup group = new IUserGroupBuilder(CoreModelServiceHolder.get(), "TestGroup").buildAndSave();
		IReminder groupReminder = new IReminderBuilder(CoreModelServiceHolder.get(), null, Visibility.ALWAYS,
				ProcessStatus.OPEN, "test").build();
		groupReminder.setGroup(group);
		CoreModelServiceHolder.get().save(groupReminder);

		results = client.search().byUrl("Task?owner=" + group.getId() + "&status:not=COMPLETED")
				.returnBundle(Bundle.class).execute();
		assertEquals(1, results.getEntry().size());

		CoreModelServiceHolder.get().remove(groupReminder);
		CoreModelServiceHolder.get().remove(group);

		List<IReminder> allPopupRemindersNotClosed = query.execute().stream()
				.filter(r -> (r.getVisibility() == Visibility.POPUP_ON_LOGIN
						|| r.getVisibility() == Visibility.POPUP_ON_PATIENT_SELECTION)
						&& r.getStatus() != ProcessStatus.CLOSED)
				.toList();
		IReminder popupReminder = new IReminderBuilder(CoreModelServiceHolder.get(), null, Visibility.POPUP_ON_LOGIN,
				ProcessStatus.OPEN, "test").buildAndSave();

		results = client.search().byUrl("Task?code=http://www.elexis.info/task/visibility|popup&status:not=COMPLETED")
				.returnBundle(Bundle.class).execute();
		assertEquals(allPopupRemindersNotClosed.size() + 1, results.getEntry().size());

		CoreModelServiceHolder.get().remove(popupReminder);
	}

	@Test
	public void testCreate() {
		IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
		List<IReminder> allReminders = query.execute();

		Task task = new Task();
		task.setIntent(TaskIntent.UNKNOWN);

		MethodOutcome outcome = null;
		boolean exHappened = false;
		try {
			outcome = client.create().resource(task).execute();
		} catch (Exception e) {
			exHappened = true;
		}
		assertTrue(exHappened);

		task.setStatus(TaskStatus.DRAFT);
		outcome = client.create().resource(task).execute();
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getResource());

		Optional<IReminder> createdLocalReminder = CoreModelServiceHolder.get()
				.load(outcome.getResource().getIdElement().getIdPart(),
				IReminder.class);
		assertTrue(createdLocalReminder.isPresent());
		CoreModelServiceHolder.get().remove(createdLocalReminder.get());

		task.setOwner(new Reference("Practitioner/" + allReminders.get(0).getResponsible().get(0).getId()));
		task.setFor(new Reference("Patient/" + allReminders.get(0).getContact().getId()));
		outcome = client.create().resource(task).execute();
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getResource());
		assertTrue(((Task) outcome.getResource()).hasOwner());
		assertEquals(allReminders.get(0).getResponsible().get(0).getId(),
				((Task) outcome.getResource()).getOwner().getReferenceElement().getIdPart());
		assertTrue(((Task) outcome.getResource()).hasFor());
		assertEquals(allReminders.get(0).getContact().getId(),
				((Task) outcome.getResource()).getFor().getReferenceElement().getIdPart());

		createdLocalReminder = CoreModelServiceHolder.get().load(outcome.getResource().getIdElement().getIdPart(),
				IReminder.class);
		assertTrue(createdLocalReminder.isPresent());
		CoreModelServiceHolder.get().remove(createdLocalReminder.get());
	}

	@Test
	public void testUpdate() {
		IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
		List<IReminder> allReminders = query.execute();

		Task task = new Task();
		task.setIntent(TaskIntent.UNKNOWN);
		task.setStatus(TaskStatus.DRAFT);
		MethodOutcome outcome = client.create().resource(task).execute();
		assertTrue(outcome.getCreated());
		assertNotNull(outcome.getResource());
		assertFalse(((Task) outcome.getResource()).hasOwner());
		assertFalse(((Task) outcome.getResource()).hasFor());
		task = (Task) outcome.getResource();

		Date due = Date.from(LocalDate.now().plusWeeks(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
		task.setStatus(TaskStatus.INPROGRESS);
		task.setExecutionPeriod(new Period().setEnd(due));
		task.setOwner(new Reference("Practitioner/" + allReminders.get(0).getResponsible().get(0).getId()));
		task.setFor(new Reference("Patient/" + allReminders.get(0).getContact().getId()));

		outcome = client.update().resource(task).execute();
		assertNotNull(outcome.getResource());
		assertEquals(TaskStatus.INPROGRESS, ((Task) outcome.getResource()).getStatus());
		assertEquals(allReminders.get(0).getResponsible().get(0).getId(),
				((Task) outcome.getResource()).getOwner().getReferenceElement().getIdPart());
		assertEquals(allReminders.get(0).getContact().getId(),
				((Task) outcome.getResource()).getFor().getReferenceElement().getIdPart());
		assertEquals(due, ((Task) outcome.getResource()).getExecutionPeriod().getEnd());
		task = (Task) outcome.getResource();

		task.setOwner(new Reference("CareTeam/ALL"));
		outcome = client.update().resource(task).execute();
		assertNotNull(outcome.getResource());
		assertEquals("ALL", ((Task) outcome.getResource()).getOwner().getReferenceElement().getIdPart());

		Optional<IReminder> createdLocalReminder = CoreModelServiceHolder.get()
				.load(outcome.getResource().getIdElement().getIdPart(), IReminder.class);
		assertTrue(createdLocalReminder.isPresent());
		CoreModelServiceHolder.get().remove(createdLocalReminder.get());
	}
}
