package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.DocHandle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.DocHandle_;

public class DocHandleService extends AbstractService<DocHandle> {

	public static DocHandleService INSTANCE = InstanceHolder.INSTANCE;

	private static final class InstanceHolder {
		static final DocHandleService INSTANCE = new DocHandleService();
	}

	private DocHandleService() {
		super(DocHandle.class);
	}

	public List<String> getAllCategories() {
		CriteriaQuery<DocHandle> cq = cb.createQuery(DocHandle.class);
		Root<DocHandle> root = cq.from(DocHandle.class);
		Predicate like = cb.like(root.get(DocHandle_.category), "%text/category%");
		cq = cq.where(like);
		TypedQuery<DocHandle> q = em.createQuery(cq);
		List<DocHandle> resultList = q.getResultList();
		return resultList.stream().map(r -> r.getCategory()).distinct().collect(Collectors.toList());
	}

}
