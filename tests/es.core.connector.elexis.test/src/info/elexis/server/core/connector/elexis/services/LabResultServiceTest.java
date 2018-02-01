package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.LocalDateTime;

import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Test;

import ch.elexis.core.types.PathologicDescription.Description;
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

}
