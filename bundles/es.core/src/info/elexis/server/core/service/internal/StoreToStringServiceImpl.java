package info.elexis.server.core.service.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.elexis.server.core.service.StoreToStringContribution;
import info.elexis.server.core.service.StoreToStringService;

@Component
public class StoreToStringServiceImpl implements StoreToStringService {

	private static Logger logger = LoggerFactory.getLogger(StoreToStringServiceImpl.class);

	private List<StoreToStringContribution> contributions;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public void bindContribution(StoreToStringContribution contribution) {
		if (contributions == null) {
			contributions = new ArrayList<>();
		}
		logger.info("Adding contribution " + contribution);
		contributions.add(contribution);
	}

	public void unbindContribution(StoreToStringContribution contribution) {
		if (contributions == null) {
			contributions = new ArrayList<>();
		}
		logger.info("Removing contribution " + contribution);
		contributions.remove(contribution);
	}

	@Override
	public Optional<Object> createFromString(String storeToString) {
		for (StoreToStringContribution storeToStringContribution : contributions) {
			Optional<Object> ret = storeToStringContribution.createFromString(storeToString);
			if (ret.isPresent()) {
				return ret;
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> storeToString(Object object) {
		for (StoreToStringContribution storeToStringContribution : contributions) {
			Optional<String> ret = storeToStringContribution.storeToString(object);
			if (ret.isPresent()) {
				return ret;
			}
		}
		return Optional.empty();
	}
}
