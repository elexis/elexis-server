package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Eigenleistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Eigenleistung_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class EigenleistungService extends PersistenceService {

	public static class Builder extends AbstractBuilder<Eigenleistung> {
		public Builder(String code) {
			object = new Eigenleistung();
			object.setCode(code);
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Eigenleistung> load(String id) {
		return PersistenceService.load(Eigenleistung.class, id).map(v -> (Eigenleistung) v);
	}

	public static Optional<? extends AbstractDBObjectIdDeleted> findByCode(String itemCode) {
		JPAQuery<Eigenleistung> query = new JPAQuery<Eigenleistung>(Eigenleistung.class);
		query.add(Eigenleistung_.code, QUERY.EQUALS, itemCode.trim());
		return query.executeGetSingleResult();
	}
}
