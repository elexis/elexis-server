package info.elexis.server.core.connector.elexis.services;

import java.util.Optional;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Eigenleistung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Eigenleistung_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class EigenleistungService extends AbstractService<Eigenleistung> {
	public static EigenleistungService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final EigenleistungService INSTANCE = new EigenleistungService();
	}

	private EigenleistungService() {
		super(Eigenleistung.class);
	}

	public static Optional<? extends AbstractDBObjectIdDeleted> findByCode(String itemCode) {
		JPAQuery<Eigenleistung> query = new JPAQuery<Eigenleistung>(Eigenleistung.class);
		query.add(Eigenleistung_.code, QUERY.EQUALS, itemCode.trim());
		return query.executeGetSingleResult();
	}
}
