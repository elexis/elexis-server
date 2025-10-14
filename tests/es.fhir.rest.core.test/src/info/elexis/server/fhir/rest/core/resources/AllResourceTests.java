package info.elexis.server.fhir.rest.core.resources;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ AllergyIntoleranceTest.class, PersonResourceProviderTest.class, PatientResourceProviderTest.class,
		OrganizationResourceProviderTest.class, AppointmentResourceProviderTest.class, CoverageTest.class, ScheduleResourceProviderTest.class,
		PractitionerRoleTest.class, ConditionResourceProviderTest.class, ObservationResourceProviderTest.class, FamilyMemberHistoryTest.class,
		EncounterResourceProviderTest.class, ServiceRequestTest.class, MedicationRequestResourceProviderTest.class, ClaimTest.class,
		DocumentReferenceResourceProviderTest.class, SubscriptionResourceTest.class, PlainResourceProviderTest.class,
		ImmunizationTest.class, TaskResourceTest.class, CareTeamTest.class, MedicationTest.class })
public class AllResourceTests {

}
