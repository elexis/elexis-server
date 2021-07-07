package es.fhir.rest.core.resources.util;

import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;

public class QueryUtil {
	
	private QueryUtil(){}
	
	public static COMPARATOR prefixParamToToQueryParam(ParamPrefixEnum prefix){
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
	
	public static ORDER sortOrderEnumToLocal(SortOrderEnum order){
		return (SortOrderEnum.ASC == order) ? ORDER.ASC : ORDER.DESC;
	}
	
}
