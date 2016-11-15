package info.elexis.server.core.connector.elexis.services;

import java.util.Collections;
import java.util.List;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel_;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class ArtikelService extends AbstractService<Artikel> {

	public static ArtikelService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final ArtikelService INSTANCE = new ArtikelService();
	}

	private ArtikelService() {
		super(Artikel.class);
	}

	/**
	 * 
	 * @param name
	 * @param internalName
	 * @param articleType
	 *            the article type class, e.g. Artikel#TYP_EIGENARTIKEL
	 * @return
	 */
	public Artikel create(String name, String internalName, String articleType) {
		em.getTransaction().begin();
		Artikel art = create(false);
		art.setName(name);
		art.setNameIntern(internalName);
		art.setTyp(articleType);
		em.getTransaction().commit();
		return art;
	}

	/**
	 * 
	 * @param eigenartikel
	 *            an Artikel of {@link Artikel#TYP_EIGENARTIKEL}
	 * @return all packages registered for an Eigenartikel (including deleted),
	 *         or an empty list of not applicable
	 */
	public static List<Artikel> eigenartikelGetPackagesForProduct(Artikel eigenartikel) {
		if (!Artikel.TYP_EIGENARTIKEL.equals(eigenartikel.getTyp())) {
			// not an "Eigenartikel"
			return Collections.emptyList();
		}

		JPAQuery<Artikel> qbe = new JPAQuery<Artikel>(Artikel.class, true);
		qbe.add(Artikel_.extId, QUERY.EQUALS, eigenartikel.getId());
		return qbe.execute();
	}

	/**
	 * Marks an Eigenartikel product and all of its associated children as
	 * deleted
	 * 
	 * @param eigenartikel
	 */
	public static void eigenartikelDeleteProductIncludingPackages(Artikel eigenartikel) {
		List<Artikel> packagesForEigenartikelProduct = eigenartikelGetPackagesForProduct(eigenartikel);
		for (Artikel artikel : packagesForEigenartikelProduct) {
			artikel.setDeleted(true);
			ArtikelService.INSTANCE.write(artikel);
		}
		eigenartikel.setDeleted(true);
	}

	/**
	 * Determine if an article is an Eigenartikel of type product
	 * 
	 * @param article
	 * @return
	 */
	public static boolean eigenartikelDetermineIfProduct(Artikel article) {
		if (article != null && Artikel.TYP_EIGENARTIKEL.equals(article.getTyp())) {
			return (article.getExtId() == null || article.getExtId().length() == 0);
		}
		return false;
	}
}
