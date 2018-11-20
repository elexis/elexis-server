//package info.elexis.server.core.connector.elexis.services;
//
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
//
//public abstract class AbstractBuilder<T extends AbstractDBObjectIdDeleted> {
//
//	public T object;
//	
//	public T build() {
//		return object;
//	}
//	
//	@SuppressWarnings("unchecked")
//	public T buildAndSave() {
//		build();
//		return (T) PersistenceService.save(object);
//	}
//}
