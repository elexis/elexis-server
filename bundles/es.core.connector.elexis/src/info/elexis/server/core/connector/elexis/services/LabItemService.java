package info.elexis.server.core.connector.elexis.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import bsh.EvalError;
import bsh.Interpreter;
import ch.elexis.core.constants.TextContainerConstants;
import ch.elexis.core.model.IContact;
import ch.elexis.core.types.LabItemTyp;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import info.elexis.server.core.connector.elexis.internal.ElexisEntityManager;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabItem_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.LabResult_;
import info.elexis.server.core.connector.elexis.map.PersistentObjectAttributeMapping;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class LabItemService extends PersistenceService {

	private static Logger log = LoggerFactory.getLogger(LabItemService.class);

	private static final Pattern varPattern = Pattern.compile(TextContainerConstants.MATCH_TEMPLATE);

	public static class Builder extends AbstractBuilder<LabItem> {
		public Builder(String code, String name, IContact laboratory, String refMale, String refFemale, String unit,
				LabItemTyp type, String group, int seq) {
			object = new LabItem();
			object.setCode(code);
			object.setName(name);
			object.setLabor((Kontakt) laboratory);
			object.setReferenceMale(refMale);
			object.setReferenceFemale(refFemale);
			object.setUnit(unit);
			object.setTyp(type);
			object.setGroup(group);
			object.setPriority(Integer.toString(seq));
			object.setVisible(true);
		}
	}

	/**
	 * convenience method
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<LabItem> load(String id) {
		return PersistenceService.load(LabItem.class, id).map(v -> (LabItem) v);
	}

	public static List<LabResult> findAllLabResultsForPatientWithType(Kontakt patient, LabItemTyp lit,
			boolean includeDeleted) {
		EntityManager em = ElexisEntityManager.createEntityManager();
		try {
			CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
			CriteriaQuery<LabResult> criteriaQuery = criteriaBuilder.createQuery(LabResult.class);
			Root<LabResult> labResult = criteriaQuery.from(LabResult.class);
			Join<LabResult, LabItem> labResultJoin = labResult.join(LabResult_.item);

			Predicate predicate = criteriaBuilder
					.and(criteriaBuilder.equal(labResult.get(LabResult_.patient), patient));
			predicate = criteriaBuilder.and(predicate,
					criteriaBuilder.equal(labResultJoin.get(LabItem_.type), Integer.toString(lit.getType())));
			if (!includeDeleted) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(labResult.get(AbstractDBObjectIdDeleted_.deleted), false));
			}

			criteriaQuery.where(predicate);
			return em.createQuery(criteriaQuery).getResultList();
		} finally {
			em.close();
		}
	}

	public static String evaluate(LabItem labItem, Kontakt patient, List<LabResult> labresults) {
		if (LabItemTyp.FORMULA != labItem.getTyp()) {
			return null;
		}
		String formula = labItem.getFormula();
		log.trace("Evaluating formula [" + formula + "]");
		if (formula.startsWith("SCRIPT:")) {
			log.warn("Script elements currently not supported, returning empty String. LabItem [" + labItem.getId()
					+ "]");
			return "";
		}
		boolean bMatched = false;
		labresults = sortResultsDescending(labresults);
		for (LabResult result : labresults) {
			String var = ((LabItem) result.getItem()).getVariableName();
			if (formula.indexOf(var) != -1) {
				if (result.getResult() != null && !result.getResult().isEmpty() && !result.getResult().equals("?")) { //$NON-NLS-1$
					formula = formula.replaceAll(var, result.getResult());
					bMatched = true;
				}
			}
		}

		// Suche Variablen der Form [Patient.Alter]
		Matcher matcher = varPattern.matcher(formula);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String var = matcher.group();
			String[] fields = var.split("\\."); //$NON-NLS-1$
			if (fields.length > 1) {
				String val = PersistentObjectAttributeMapping.get(patient,
						fields[1].replaceFirst("\\]", StringTool.leer));
				String repl = "\"" + val + "\"";
				matcher.appendReplacement(sb, repl);
				bMatched = true;
			}
		}
		matcher.appendTail(sb);
		if (!bMatched) {
			return null;
		}

		try {
			Interpreter bshInterpreter = new bsh.Interpreter();
			return bshInterpreter.eval(sb.toString()).toString();
		} catch (Exception e) {
			if (e instanceof EvalError) {
				log.info("Error evaluating formula [{}], returning ?formel?.", sb.toString());
			} else {
				log.warn("Error evaluating formula [{}], returning ?formel?.", sb.toString(), e);
			}
			return "?formel?";
		}
	}

	public static String evaluate(LabItem labItem, Kontakt patient, TimeTool date) {
		if (LabItemTyp.FORMULA != labItem.getTyp()) {
			return null;
		}

		JPAQuery<LabResult> qbe = new JPAQuery<LabResult>(LabResult.class);
		qbe.add(LabResult_.patient, QUERY.EQUALS, patient);
		qbe.add(LabResult_.date, QUERY.EQUALS, date.toLocalDate());
		List<LabResult> results = qbe.execute();
		return evaluate(labItem, patient, results);
	}

	private static List<LabResult> sortResultsDescending(List<LabResult> results) {
		Collections.sort(results, new Comparator<LabResult>() {
			@Override
			public int compare(LabResult lr1, LabResult lr2) {
				int var1Length = ((LabItem) lr1.getItem()).getVariableName().length();
				int var2Length = ((LabItem) lr2.getItem()).getVariableName().length();

				if (var1Length < var2Length) {
					return 1;
				} else if (var1Length > var2Length) {
					return -1;
				}
				return 0;
			}
		});
		return results;
	}

}
