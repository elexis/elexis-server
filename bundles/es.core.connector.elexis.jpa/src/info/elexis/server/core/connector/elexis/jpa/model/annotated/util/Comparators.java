package info.elexis.server.core.connector.elexis.jpa.model.annotated.util;

import java.math.BigInteger;
import java.util.Comparator;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;

public class Comparators {
	private Comparators() {
		throw new AssertionError("no instances");
	}

	/**
	 * Sort ascending by lastUpdate. Use {@link #reversed()} to sort descending by
	 * lastUpdate
	 */
	public enum LastUpdateOrderComparator implements Comparator<AbstractDBObject> {
		INSTANCE;

		@Override
		public int compare(AbstractDBObject o1, AbstractDBObject o2) {
			BigInteger l1 = (o1.getLastupdate() != null) ? o1.getLastupdate() : BigInteger.ZERO;
			BigInteger l2 = (o2.getLastupdate() != null) ? o2.getLastupdate() : BigInteger.ZERO;
			return l1.compareTo(l2);
		}

	}

}
