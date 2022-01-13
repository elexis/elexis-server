package es.fhir.rest.core.resources.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.hl7.fhir.r4.model.Patient;

import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import es.fhir.rest.core.resources.util.SearchFilterParser.CompareOperation;
import es.fhir.rest.core.resources.util.SearchFilterParser.Filter;
import es.fhir.rest.core.resources.util.SearchFilterParser.FilterLogical;
import es.fhir.rest.core.resources.util.SearchFilterParser.FilterParameter;
import es.fhir.rest.core.resources.util.SearchFilterParser.FilterParameterPath;
import es.fhir.rest.core.resources.util.SearchFilterParser.FilterSyntaxException;

public class IContactSearchFilterQueryAdapter {

	public void adapt(IQuery<? extends IContact> query, StringAndListParam theFtFilter) {

		List<StringOrListParam> stringOrListParams = theFtFilter.getValuesAsQueryTokens();
		if (!stringOrListParams.isEmpty()) {
			List<StringParam> stringParams = stringOrListParams.get(0).getValuesAsQueryTokens();
			if (!stringParams.isEmpty()) {
				StringParam stringParam = stringParams.get(0);

				try {
					Filter filter = SearchFilterParser.parse(stringParam.getValue());
					handleFilter(query, filter, 1);

				} catch (FilterSyntaxException | IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

	}

	private void handleFilter(IQuery<? extends IContact> query, Filter filter, int op) {

		if (filter instanceof FilterParameter) {
			// e.g. _filter=email eq "mymail@address.ch"
			FilterParameter filterParameter = (FilterParameter) filter;

			// this may resolve to multiple database entries?!

			Set<EStructuralFeature> translateParamPath = translateParamPath(filterParameter.getParamPath());
			if (translateParamPath.size() > 1) {
				query.startGroup();
			}
			for (EStructuralFeature eStructuralFeature : translateParamPath) {
				String value = filterParameter.getValue();
				if (CompareOperation.co == filterParameter.getOperation()) {
					value = "%" + value + "%";
				}

				if (op == 1) {
					query.and(eStructuralFeature, translateOperation(filterParameter.getOperation()), value, true);
				}
				if (op == 2) {
					query.or(eStructuralFeature, translateOperation(filterParameter.getOperation()), value, true);
				}
			}
			if (translateParamPath.size() > 1) {
				// query.orJoinGroups();
			}

		} else if (filter instanceof FilterLogical) {
			// _filter=identifier eq "www.elexis.info%2Fpatnr%7C11223" or address co "11223"
			// / ??
			FilterLogical filterLogical = (FilterLogical) filter;
			query.startGroup();

			handleFilter(query, filterLogical.getFilter1(), 2);
			handleFilter(query, filterLogical.getFilter2(), 2);

			query.andJoinGroups();

			// if(filterLogical.getOperation().equals(FilterLogicalOperation.or)) {
			// query.orJoinGroups();
			// }
		}

	}

	private COMPARATOR translateOperation(CompareOperation operation) {
		switch (operation) {
		case eq:
			return COMPARATOR.EQUALS;
		case ne:
			return COMPARATOR.NOT_EQUALS;
		case co:
			return COMPARATOR.LIKE;
		default:
			break;
		}
		throw new IllegalArgumentException("Illegal op: " + operation);
	}

	private Set<EStructuralFeature> translateParamPath(FilterParameterPath paramPath) {
		switch (paramPath.getName()) {
		case Patient.SP_NAME:
			return Set.of(ModelPackage.Literals.ICONTACT__DESCRIPTION1, ModelPackage.Literals.ICONTACT__DESCRIPTION2);
		case Patient.SP_EMAIL:
			return Collections.singleton(ModelPackage.Literals.ICONTACT__EMAIL);
		case Patient.SP_BIRTHDATE:
			return Collections.singleton(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH);
		case Patient.SP_ADDRESS:
			return Collections.singleton(ModelPackage.Literals.ICONTACT__STREET);
		default:
			break;
		}
		throw new IllegalArgumentException("Illegal paramPath: " + paramPath.getName());
	}

}
