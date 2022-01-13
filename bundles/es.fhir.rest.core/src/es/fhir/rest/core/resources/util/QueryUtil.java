package es.fhir.rest.core.resources.util;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ca.uhn.fhir.rest.param.StringParam;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;

public class QueryUtil {

	private QueryUtil() {
	}

	public static COMPARATOR prefixParamToToQueryParam(ParamPrefixEnum prefix) {
		switch (prefix) {
		case EQUAL:
			return COMPARATOR.EQUALS;
		case GREATERTHAN:
			return COMPARATOR.GREATER;
		case GREATERTHAN_OR_EQUALS:
			return COMPARATOR.GREATER_OR_EQUAL;
		case LESSTHAN:
			return COMPARATOR.LESS;
		case LESSTHAN_OR_EQUALS:
			return COMPARATOR.LESS_OR_EQUAL;
		case NOT_EQUAL:
			return COMPARATOR.NOT_EQUALS;
		default:
			break;
		}
		throw new UnsupportedOperationException();
	}

	public static ORDER sortOrderEnumToLocal(SortOrderEnum order) {
		return (SortOrderEnum.ASC == order) ? ORDER.ASC : ORDER.DESC;
	}

	public static void andContactNameCriterion(IQuery<? extends IContact> query, StringParam name) {

		String value = name.getValue();
		if (name.isContains()) {
			value = "%" + value + "%";
		}

		query.startGroup();
		query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, value, true);
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.LIKE, value, true);
		query.andJoinGroups();
	}

}
