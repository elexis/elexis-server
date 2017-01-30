package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Heap;

public class HeapService extends PersistenceService {

	public static class Builder extends AbstractBuilder<Heap> {
		public Builder(String id) {
			object = new Heap();
			object.setId(id);
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Heap> load(String id) {
		return PersistenceService.load(Heap.class, id).map(v -> (Heap) v);
	}
}
