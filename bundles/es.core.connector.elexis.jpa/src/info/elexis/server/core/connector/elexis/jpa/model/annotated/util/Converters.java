package info.elexis.server.core.connector.elexis.jpa.model.annotated.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObject;

public class Converters {

	public Optional<ZonedDateTime> getLastUpdateAsZonedDateTime(AbstractDBObject ado) {
		if (ado.getLastupdate() != null) {
			ZonedDateTime zonedDateTime = Instant.ofEpochMilli(ado.getLastupdate().longValue())
					.atZone(ZoneId.systemDefault());
			return Optional.of(zonedDateTime);

		}
		return Optional.empty();
	}

	public Optional<Date> getLastUpdateAsDate(AbstractDBObject ado) {
		if (getLastUpdateAsZonedDateTime(ado).isPresent()) {
			Date lastUpdateDate = Date.from(getLastUpdateAsZonedDateTime(ado).get().toInstant());
			return Optional.of(lastUpdateDate);
		}
		return Optional.empty();
	}

}
