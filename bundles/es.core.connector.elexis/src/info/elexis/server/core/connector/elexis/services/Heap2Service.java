package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Heap2;

public class Heap2Service extends AbstractService<Heap2> {

	public static Heap2Service INSTANCE = InstanceHolder.INSTANCE;
	
	private static final class InstanceHolder {
		static final Heap2Service INSTANCE = new Heap2Service();
	}

	private Heap2Service() {
		super(Heap2.class);
	}


}
