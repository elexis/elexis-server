package es.fhir.rest.core.resources.util;

import ca.uhn.fhir.rest.param.ParamPrefixEnum;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;

public class QueryUtil {

	public static QUERY prefixParamToToQueryParam(ParamPrefixEnum prefix) {
		switch (prefix) {
		case EQUAL:
			return QUERY.EQUALS;
		case GREATERTHAN:
			return QUERY.GREATER;
		case GREATERTHAN_OR_EQUALS:
			return QUERY.GREATER_OR_EQUAL;
		case LESSTHAN:
			return QUERY.LESS;
		case LESSTHAN_OR_EQUALS:
			return QUERY.LESS_OR_EQUAL;
		case NOT_EQUAL:
			return QUERY.NOT_EQUALS;
		default:
			break;
		}
		throw new UnsupportedOperationException();
	}

}
