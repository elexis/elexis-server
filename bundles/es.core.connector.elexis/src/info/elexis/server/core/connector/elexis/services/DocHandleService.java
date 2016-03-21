package info.elexis.server.core.connector.elexis.services;

import java.time.LocalDate;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.DocHandle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.DocHandle_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class DocHandleService extends AbstractService<DocHandle> {

	public static DocHandleService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final DocHandleService INSTANCE = new DocHandleService();
	}

	private DocHandleService() {
		super(DocHandle.class);
	}

	/**
	 * 
	 * @param contact
	 * @param title
	 * @param filename the filename, has to include the resp. ending on mimetype (e.g. <code>.pdf</code>);
	 * @param category
	 *            if <code>null</code> defaults to "default"
	 * @param document
	 * @return
	 */
	public DocHandle create(Kontakt contact, String title, String filename, String category,byte[] document) {
		em.getTransaction().begin();
		DocHandle docHandle = create(false);
		em.merge(contact);
		docHandle.setKontakt(contact);
		docHandle.setDatum(LocalDate.now());
		docHandle.setCategory((category != null) ? category : "default");
		docHandle.setMimetype(filename);
		docHandle.setDoc(document);
		docHandle.setTitle(title);
		em.getTransaction().commit();
		return docHandle;
	}

	public List<DocHandle> getAllCategories() {
		EntityManager em = ElexisEntityManager.createEntityManager();
		CriteriaQuery<DocHandle> cq = cb.createQuery(DocHandle.class);
		Root<DocHandle> root = cq.from(DocHandle.class);
		Predicate like = cb.like(root.get(DocHandle_.mimetype), "%text/category%");
		cq = cq.where(like);
		TypedQuery<DocHandle> q = em.createQuery(cq);
		return q.getResultList();
	}

}
