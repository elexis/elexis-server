package info.elexis.server.core.connector.elexis.services;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Userconfig;

public class UserconfigService {

	private static Logger log = LoggerFactory.getLogger(UserconfigService.class);

	public static UserconfigService INSTANCE = InstanceHolder.INSTANCE;

	protected EntityManager em;

	private static final class InstanceHolder {
		static final UserconfigService INSTANCE = new UserconfigService();
	}

	private UserconfigService() {
		em = ElexisEntityManager.createEntityManager();
	}

	/**
	 * Find an object by its primary id.
	 * 
	 * @param id
	 * @param entityClass
	 * @return
	 */
	public Userconfig findById(Object id) {
		return em.find(Userconfig.class, id);
	}

	/**
	 * 
	 * @param param
	 * @param performCommit
	 * @return
	 */
	public Userconfig create(String param, final boolean performCommit) {
		Userconfig obj = new Userconfig();
		if (performCommit)
			em.getTransaction().begin();
		if (param != null) {
			obj.setParam(param);
		}
		em.persist(obj);
		if (performCommit)
			em.getTransaction().commit();
		return obj;
	}
	
	/**
	 * Create a transaction and flush all open changes onto the database
	 */
	public void flush() {
		em.getTransaction().begin();
		em.flush();
		em.getTransaction().commit();
	}

}
