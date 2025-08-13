package info.elexis.server.fhir.rest.core.resources;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AllergyIntoleranceTest.class, PersonResourceProviderTest.class, PatientResourceProviderTest.class,
		OrganizationResourceProviderTest.class, AppointmentResourceTest.class, CoverageTest.class, ScheduleResourceTest.class,
		PractitionerRoleTest.class, ConditionTest.class, ObservationTest.class, FamilyMemberHistoryTest.class,
		EncounterTest.class, ServiceRequestTest.class, MedicationRequestTest.class, ClaimTest.class,
		DocumentReferenceTest.class, SubscriptionResourceTest.class, PlainResourceProviderTest.class,
		ImmunizationTest.class, TaskResourceTest.class, CareTeamTest.class, MedicationTest.class })
public class AllResourceTests {

}
