package info.elexis.server.fhir.rest.core.resources;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AppointmentResourceProviderTest.class, AllergyIntoleranceTest.class, CareTeamTest.class,
		ConditionResourceProviderTest.class, ClaimTest.class, CoverageTest.class,
		DocumentReferenceResourceProviderTest.class, EncounterResourceProviderTest.class, FamilyMemberHistoryTest.class,
		ImmunizationTest.class, InvoiceResourceProviderTest.class, MedicationResourceProviderTest.class,
		MedicationRequestResourceProviderTest.class, ObservationResourceProviderTest.class,
		OrganizationResourceProviderTest.class, PersonResourceProviderTest.class, PatientResourceProviderTest.class,
		PlainResourceProviderTest.class, PractitionerRoleTest.class, ScheduleResourceProviderTest.class,
		ServiceRequestTest.class, SubscriptionResourceTest.class, TaskResourceTest.class })
public class AllResourceTests {

}
