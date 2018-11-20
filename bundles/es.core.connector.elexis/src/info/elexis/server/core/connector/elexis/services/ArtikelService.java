//package info.elexis.server.core.connector.elexis.services;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
//import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel_;
//import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
//
//public class ArtikelService extends PersistenceService {
//
//	public static class Builder extends AbstractBuilder<Artikel> {
//		public Builder(String name, String internalName, String articleType) {
//			object = new Artikel();
//			object.setName(name);
//			object.setNameIntern(internalName);
//			object.setTyp(articleType);
//		}
//	}
//
//	/**
//	 * convenience method
//	 * 
//	 * @param id
//	 * @return
//	 */
//	public static Optional<Artikel> load(String id) {
//		return PersistenceService.load(Artikel.class, id).map(v -> (Artikel) v);
//	}
//
//	/**
//	 * @param article
//	 * @return whether this article is an Eigenartikel
//	 */
//	public static boolean isEigenartikel(Artikel article) {
//		return Artikel.TYP_EIGENARTIKEL.equals(article.getTyp());
//	}
//
//	/**
//	 * 
//	 * @param eigenartikel
//	 *            an Artikel of {@link Artikel#TYP_EIGENARTIKEL}
//	 * @return all packages registered for an Eigenartikel (including deleted),
//	 *         or an empty list of not applicable
//	 */
//	public static List<Artikel> eigenartikelGetPackagesForProduct(Artikel eigenartikel) {
//		if (!isEigenartikel(eigenartikel)) {
//			// not an "Eigenartikel"
//			return Collections.emptyList();
//		}
//
//		JPAQuery<Artikel> qbe = new JPAQuery<Artikel>(Artikel.class, true);
//		qbe.add(Artikel_.extId, QUERY.EQUALS, eigenartikel.getId());
//		return qbe.execute();
//	}
//
//	/**
//	 * Marks an Eigenartikel product and all of its associated children as
//	 * deleted
//	 * 
//	 * @param eigenartikel
//	 */
//	public static void eigenartikelDeleteProductIncludingPackages(Artikel eigenartikel) {
//		List<Artikel> packagesForEigenartikelProduct = eigenartikelGetPackagesForProduct(eigenartikel);
//		for (Artikel artikel : packagesForEigenartikelProduct) {
//			artikel.setDeleted(true);
//			ArtikelService.save(artikel);
//		}
//		eigenartikel.setDeleted(true);
//	}
//
//	/**
//	 * Determine if an article is an Eigenartikel of type product
//	 * 
//	 * @param article
//	 * @return
//	 */
//	public static boolean eigenartikelDetermineIfProduct(Artikel article) {
//		if (article != null && isEigenartikel(article)) {
//			return (article.getExtId() == null || article.getExtId().length() == 0);
//		}
//		return false;
//	}
//
//	public static Optional<Artikel> findByGTIN(String gtin) {
//		JPAQuery<Artikel> qbe = new JPAQuery<Artikel>(Artikel.class);
//		qbe.add(Artikel_.ean, QUERY.LIKE, gtin);
//		return qbe.executeGetSingleResult();
//	}
//}
