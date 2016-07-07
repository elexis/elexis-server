package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.types.LabItemTyp;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;

public class LabItemServiceTest {

	@Test
	public void testFindAllLabResultsForPatientWithType() {
		Optional<Kontakt> patient = KontaktService.INSTANCE.findById("i46395865ce01d37e0158");
		List<LabResult> labResults = LabItemService.findAllLabResultsForPatientWithType(patient.get(),
				LabItemTyp.FORMULA, false);
		assertEquals(1, labResults.size());
	}

}
