package info.elexis.server.findings.fhir.jpa.codes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.codes.ICodingContribution;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.core.model.IBillingSystemFactor;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import info.elexis.server.findings.fhir.jpa.service.CoreModelServiceHolder;

@Component
public class CoverageTypeCodingContribution implements ICodingContribution {

	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private IModelService modelService;
		
	private List<ICoding> codes;

	@Override
	public String getCodeSystem() {
		return CodingSystem.ELEXIS_COVERAGE_TYPE.getSystem();
	}

	@Override
	public List<ICoding> getCodes() {
		if (codes == null) {
			codes = loadTypes();
		}
		return codes;
	}

	private List<ICoding> loadTypes() {
		List<ICoding> ret = new ArrayList<>();
		IQuery<IBillingSystemFactor> query = CoreModelServiceHolder.get().getQuery(IBillingSystemFactor.class);
		List<IBillingSystemFactor> factors = query.execute();
		HashSet<String> uniqueTypes = new HashSet<>();
		for (IBillingSystemFactor factor : factors) {
			uniqueTypes.add(factor.getSystem());
		}
		if (!uniqueTypes.isEmpty()) {
			for (String string : uniqueTypes) {
				ret.add(new TransientCoding(getCodeSystem(), string, string));
			}
		}
		return ret;
	}

	@Override
	public Optional<ICoding> getCode(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
}
