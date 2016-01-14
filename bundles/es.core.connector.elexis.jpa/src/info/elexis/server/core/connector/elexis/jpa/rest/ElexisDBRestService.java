package info.elexis.server.core.connector.elexis.jpa.rest;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import info.elexis.server.core.connector.elexis.jpa.manager.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

@Path("/contacts")
public class ElexisDBRestService {

	@GET
	public String getStatus() {
		EntityManager em = ElexisEntityManager.em();
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Kontakt> cq = cb.createQuery(Kontakt.class);
		Root<Kontakt> rootEntry = cq.from(Kontakt.class);
		CriteriaQuery<Kontakt> all = cq.select(rootEntry);
		TypedQuery<Kontakt> allQuery = em.createQuery(all);
		return allQuery.getResultList().size() + " contacts found.";
	}

	@POST
	@Consumes(MediaType.APPLICATION_XML)
	public void create(Kontakt contact) {
		ElexisEntityManager.em().persist(contact);
	}

	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Path("{id}")
	public Kontakt read(@PathParam("id") String id) {
		ElexisEntityManager.getEntityManagerFactory().getCache().evictAll();
		return ElexisEntityManager.em().find(Kontakt.class, id);
	}

	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public void update(Kontakt customer) {
		ElexisEntityManager.em().merge(customer);
	}

	// @DELETE
	// @Path("{id}")
	// public void delete(@PathParam("id") long id) {
	// Kontakt customer = read(id);
	// if (null != customer) {
	// entityManager.remove(customer);
	// }
	// }

	// @GET
	// @Produces(MediaType.APPLICATION_XML)
	// @Path("findCustomersByCity/{city}")
	// public List<Kontakt> findCustomersByCity(@PathParam("city") String city)
	// {
	// Query query = entityManager.createNamedQuery("findCustomersByCity");
	// query.setParameter("city", city);
	// return query.getResultList();
	// }
}
