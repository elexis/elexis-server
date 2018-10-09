package info.elexis.server.core.connector.elexis.services;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.TextContainerConstants;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;


public class LabItemService extends PersistenceService2 {

	private static Logger log = LoggerFactory.getLogger(LabItemService.class);

	private static final Pattern varPattern = Pattern.compile(TextContainerConstants.MATCH_TEMPLATE);


//	public static List<ILabResult> findAllLabResultsForPatientWithType(IPatient patient, LabItemTyp lit,
//			boolean includeDeleted) {
//		EntityManager em = ElexisEntityManager.createEntityManager();
//		try {
//			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
//			CriteriaQuery<LabResult> criteriaQuery = criteriaBuilder.createQuery(LabResult.class);
//			Root<LabResult> labResult = criteriaQuery.from(LabResult.class);
//			Join<LabResult, LabItem> labResultJoin = labResult.join(LabResult_.item);
//
//			Predicate predicate = criteriaBuilder
//					.and(criteriaBuilder.equal(labResult.get(LabResult_.patient), patient));
//			predicate = criteriaBuilder.and(predicate,
//					criteriaBuilder.equal(labResultJoin.get(LabItem_.type), Integer.toString(lit.getType())));
//			if (!includeDeleted) {
//				predicate = criteriaBuilder.and(predicate,
//						criteriaBuilder.equal(labResult.get(AbstractDBObjectIdDeleted_.deleted), false));
//			}
//
//			criteriaQuery.where(predicate);
//			return em.createQuery(criteriaQuery).getResultList();
//		} finally {
//			em.close();
//		}
//	}

	public static ILabMapping findLabMappingByContactAndItemName(IContact origin, String itemName){
		IQuery<ILabMapping> qbe = modelService.getQuery(ILabMapping.class);
		qbe.and(ModelPackage.Literals.ILAB_MAPPING__ORIGIN, COMPARATOR.EQUALS, origin);
		qbe.and(ModelPackage.Literals.ILAB_MAPPING__ITEM_NAME, COMPARATOR.EQUALS, itemName);
		List<ILabMapping> res = qbe.execute();
		if (res.isEmpty()) {
			return null;
		} else {
			if (res.size() > 1) {
				throw new IllegalArgumentException(
					String.format("Found more then 1 mapping for origin id [%s] - [%s]", //$NON-NLS-1$
						origin.getId(), itemName));
			}
			return res.get(0);
		}
	}

	public static String findItemNameForLabItemByOrigin(ILabItem labItem, IContact origin) {
		IQuery<ILabMapping> qbe = modelService.getQuery(ILabMapping.class);
		qbe.and(ModelPackage.Literals.ILAB_MAPPING__ORIGIN, COMPARATOR.EQUALS, origin);
		qbe.and(ModelPackage.Literals.ILAB_MAPPING__ITEM, COMPARATOR.EQUALS, labItem);
		List<ILabMapping> res = qbe.execute();
		if (res.size() == 1) {
			return res.get(0).getItemName();
		}
		return null;
	}

	public static void addLabMapping(ILabItem labItem, IContact origin, String itemName){
		ILabMapping existing = findLabMappingByContactAndItemName(origin, itemName);
		if (existing != null) {
			throw new IllegalArgumentException(String.format(
				"Mapping for origin id [%s] - [%s] already exists can not create multiple instances.", //$NON-NLS-1$
				origin.getId(), itemName));
		}
		
		ILabMapping labMapping = modelService.create(ILabMapping.class);
		labMapping.setItemName(itemName);
		labMapping.setOrigin(origin);
		labMapping.setItem(labItem);
		modelService.save(labMapping);
	}

}
