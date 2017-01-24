package info.elexis.server.core.connector.elexis.jpa;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.service.StoreToStringContribution;

@Component
public class StoreToStringContibution implements StoreToStringContribution {

	@Override
	public Optional<Object> createFromString(String storeToString) {
		return StoreToStringService.INSTANCE.createDetachedFromString(storeToString).map(r -> Optional.of(r));
	}

	@Override
	public Optional<String> storeToString(Object object) {
		if (object instanceof AbstractDBObjectIdDeleted) {
			return Optional.ofNullable(StoreToStringService.storeToString((AbstractDBObjectIdDeleted) object));
		}
		return Optional.empty();
	}
}
