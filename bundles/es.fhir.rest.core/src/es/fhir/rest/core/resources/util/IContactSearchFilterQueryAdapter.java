package es.fhir.rest.core.resources.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueType;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import es.fhir.rest.core.resources.util.SearchFilterParser.CompareOperation;
import es.fhir.rest.core.resources.util.SearchFilterParser.Filter;
import es.fhir.rest.core.resources.util.SearchFilterParser.FilterLogical;
import es.fhir.rest.core.resources.util.SearchFilterParser.FilterLogicalOperation;
import es.fhir.rest.core.resources.util.SearchFilterParser.FilterParameter;
import es.fhir.rest.core.resources.util.SearchFilterParser.FilterParameterPath;

public class IContactSearchFilterQueryAdapter {

	public void adapt(IQuery<? extends IContact> query, StringAndListParam theFtFilter) {

		List<StringOrListParam> stringOrListParams = theFtFilter.getValuesAsQueryTokens();
		if (!stringOrListParams.isEmpty()) {
			List<StringParam> stringParams = stringOrListParams.get(0).getValuesAsQueryTokens();
			if (!stringParams.isEmpty()) {
				StringParam stringParam = stringParams.get(0);

				try {
					Filter filter = new SearchFilterParser(stringParam.getValue()).parse();
					handleFilter(query, filter);

				} catch (Exception e) {
					LoggerFactory.getLogger(getClass()).warn("input is [{}]", stringParam.getValue());
					OperationOutcome opOutcome = generateOperationOutcome(e);
					throw new PreconditionFailedException(e.getMessage(), opOutcome);
				}
			}
		}

	}

	private OperationOutcome generateOperationOutcome(Exception e) {
		OperationOutcome opOutcome = new OperationOutcome();
		OperationOutcomeIssueComponent ooc = new OperationOutcomeIssueComponent();
		OperationOutcome.IssueSeverity severity = OperationOutcome.IssueSeverity.ERROR;
		ooc.setSeverity(severity);
		ooc.setCode(IssueType.PROCESSING);
		ooc.setDiagnostics("_filter parameter: " + e.getMessage());
		opOutcome.addIssue(ooc);
		return opOutcome;
	}

	private void handleFilter(IQuery<? extends IContact> query, Filter filter) {

		if (filter instanceof FilterParameter) {
			// e.g. _filter=email eq "mymail@address.ch"
			FilterParameter filterParameter = (FilterParameter) filter;

			// this may resolve to multiple database entries?!

			Set<EStructuralFeature> translateParamPath = translateParamPath(filterParameter.getParamPath());
			if (translateParamPath.size() > 1) {
				query.startGroup();
			}

			int op = (filterParameter.getOperation() == CompareOperation.co) ? 2 : 1;

			for (EStructuralFeature eStructuralFeature : translateParamPath) {
				Object value = translateValue(filterParameter, eStructuralFeature);
				if (CompareOperation.co == filterParameter.getOperation()) {
					// TODO only if value instanceof String
					value = "%" + value.toString() + "%";
				}

				if (op == 1) {
					query.and(eStructuralFeature, translateOperation(filterParameter.getOperation()), value, true);
				}
				if (op == 2) {
					query.or(eStructuralFeature, translateOperation(filterParameter.getOperation()), value, true);
				}
			}

		} else if (filter instanceof FilterLogical) {
			// _filter=identifier eq "www.elexis.info%2Fpatnr%7C11223" or address co "11223"
			FilterLogical filterLogical = (FilterLogical) filter;

			query.startGroup();
			handleFilter(query, filterLogical.getFilter1());
			handleFilter(query, filterLogical.getFilter2());
			if (filterLogical.getOperation() == FilterLogicalOperation.and) {
				query.andJoinGroups();
			} else {
				query.orJoinGroups();
			}
		}

	}

	private Object translateValue(FilterParameter filterParameter, EStructuralFeature eStructuralFeature) {
		if (LocalDateTime.class == eStructuralFeature.getEType().getInstanceClass()) {
			return parseLocalDate(filterParameter.getValue());
		}
		String value = filterParameter.getValue();
		if (Objects.equals(filterParameter.getParamPath().getName(), Patient.SP_IDENTIFIER)) {
			if (StringUtils.startsWith(value, IdentifierSystem.ELEXIS_PATNR.getSystem())) {
				return StringUtils.substring(value, IdentifierSystem.ELEXIS_PATNR.getSystem().length() + 1);
			} else {
				LoggerFactory.getLogger(getClass()).error("Unsupported identifier [{}]", value);
			}
		} else if (Objects.equals(filterParameter.getParamPath().getName(), Patient.SP_PHONE)) {
			if (StringUtils.startsWith(value, "+4")) {
				// LI +423
				if (StringUtils.startsWith(value, "+423")) {
					return StringUtils.substring(value, 4);
				}
				// AT +43, CH +41, DE +49
				// cut the first 3 chars to remove country prefix
				return StringUtils.substring(value, 3);
			}
		}
		return filterParameter.getValue();
	}

	/**
	 * 
	 * @param value
	 * @return
	 * @see https://build.fhir.org/datatypes.html#date
	 */
	private LocalDate parseLocalDate(String value) {
		DateTimeDt dateTimeDt = new DateTimeDt(value);
		return LocalDate.of(dateTimeDt.getYear(), dateTimeDt.getMonth() + 1, dateTimeDt.getDay());
	}

	private COMPARATOR translateOperation(CompareOperation operation) {
		switch (operation) {
		case eq:
			return COMPARATOR.EQUALS;
		case ne:
			return COMPARATOR.NOT_EQUALS;
		case ge:
			return COMPARATOR.GREATER_OR_EQUAL;
		case gt:
			return COMPARATOR.GREATER;
		case le:
			return COMPARATOR.LESS_OR_EQUAL;
		case lt:
			return COMPARATOR.LESS;
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
			return Set.of(ModelPackage.Literals.ICONTACT__STREET, ModelPackage.Literals.ICONTACT__CITY,
					ModelPackage.Literals.ICONTACT__ZIP);
		case Patient.SP_IDENTIFIER:
			return Collections.singleton(ModelPackage.Literals.ICONTACT__CODE);
		case Patient.SP_PHONE:
		case Patient.SP_TELECOM:
			return Set.of(ModelPackage.Literals.ICONTACT__PHONE1, ModelPackage.Literals.ICONTACT__PHONE2,
					ModelPackage.Literals.ICONTACT__MOBILE);
		default:
			break;
		}
		throw new IllegalArgumentException("Illegal paramPath: " + paramPath.getName());
	}

}
