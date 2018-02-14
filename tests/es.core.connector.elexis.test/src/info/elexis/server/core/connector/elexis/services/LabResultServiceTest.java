package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;

import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.types.PathologicDescription.Description;
import info.elexis.server.core.connector.elexis.AllTestsSuite;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

public class LabResultServiceTest {

	@Test
	public void testGetLabResultById() {
		LabResult result = LabResultService.load("dafcc08ccd5e607301762").get();
		assertNotNull(result);
		LocalDateTime of = LocalDateTime.of(2015, 7, 6, 7, 30);
		MatcherAssert.assertThat(result.getObservationtime(), LocalDateTimeMatchers.sameInstant(of));
		assertEquals(Description.UNKNOWN, result.getPathologicDescription().getDescription());
	}

	@Test
	public void testfindItemNameForLabItemByOrigin() {
		LabItem labItem = AllTestsSuite.getInitializer().getLabItem();
		LabResult lr = new LabResultService.Builder(labItem, AllTestsSuite.getInitializer().getPatient()).build();
		lr.setResult("foobar");
		lr.setOrigin(AllTestsSuite.getInitializer().getLaboratory2());
		LabResultService.save(lr);

		String originItemId = LabItemService.findItemNameForLabItemByOrigin(labItem,
				AllTestsSuite.getInitializer().getLaboratory2());
		assertEquals("TEST_NUMERIC_EXT", originItemId);
		assertEquals("ZURANA", XidService.getDomainId(AllTestsSuite.getInitializer().getLaboratory2(),
				XidConstants.XID_KONTAKT_LAB_SENDING_FACILITY));
	}

}
