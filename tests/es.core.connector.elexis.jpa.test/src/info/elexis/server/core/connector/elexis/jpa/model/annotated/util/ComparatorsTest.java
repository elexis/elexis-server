package info.elexis.server.core.connector.elexis.jpa.model.annotated.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class ComparatorsTest {

	@Test
	public void testLastUpdateOrderComparator() {
		Kontakt lu1 = new Kontakt();
		lu1.setLastupdate(BigInteger.ONE);
		Kontakt lu5 = new Kontakt();
		lu5.setLastupdate(BigInteger.valueOf(5l));
		Kontakt lu12 = new Kontakt();
		lu12.setLastupdate(BigInteger.valueOf(12l));
		Kontakt luNull = new Kontakt();

		List<Kontakt> list = Arrays.asList(lu12, lu1, lu5, luNull);
		assertNull(list.stream().sorted(Comparators.LastUpdateOrderComparator.INSTANCE).findFirst().get()
				.getLastupdate());
		assertEquals(12, list.stream().sorted(Comparators.LastUpdateOrderComparator.INSTANCE.reversed()).findFirst()
				.get().getLastupdate().intValue());
	}

}
