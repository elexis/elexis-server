package info.elexis.server.core.connector.elexis.services;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Heap;

public class HeapService extends AbstractService<Heap> {

	public static HeapService INSTANCE = InstanceHolder.INSTANCE;
	
	private static final class InstanceHolder {
		static final HeapService INSTANCE = new HeapService();
	}

	private HeapService() {
		super(Heap.class);
	}
	
	@Override
	public void remove(Heap entity) {
		// TODO Auto-generated method stub
		super.remove(entity);
	}
}
