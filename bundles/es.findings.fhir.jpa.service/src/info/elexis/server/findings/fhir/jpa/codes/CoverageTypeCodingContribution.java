package info.elexis.server.findings.fhir.jpa.codes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.codes.ICodingContribution;
import ch.elexis.core.findings.util.model.TransientCoding;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.VKPreis;
import info.elexis.server.core.connector.elexis.services.JPAQuery;

@Component
public class CoverageTypeCodingContribution implements ICodingContribution {

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
		JPAQuery<VKPreis> query = new JPAQuery<>(VKPreis.class);
		List<VKPreis> preise = query.execute();
		HashSet<String> uniqueTypes = new HashSet<>();
		for (VKPreis vkPreis : preise) {
			uniqueTypes.add(vkPreis.getTyp());
		}
		if (!uniqueTypes.isEmpty()) {
			for (String string : uniqueTypes) {
				ret.add(new TransientCoding(getCodeSystem(), string, string));
			}
		}
		return ret;
	}
}
