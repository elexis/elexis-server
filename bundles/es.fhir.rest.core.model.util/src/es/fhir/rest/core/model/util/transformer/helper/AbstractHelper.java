package es.fhir.rest.core.model.util.transformer.helper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.DomainResource;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.findings.util.ModelUtil;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.Identifiable;
import info.elexis.server.core.connector.elexis.locking.LockServiceInstance;

public class AbstractHelper {
	
	private static Logger logger = LoggerFactory.getLogger(AbstractHelper.class);
	
	protected Date getDate(LocalDateTime localDateTime){
		ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}
	
	protected Date getDate(LocalDate localDate){
		ZonedDateTime zdt = localDate.atStartOfDay(ZoneId.systemDefault());
		return Date.from(zdt.toInstant());
	}
	
	protected LocalDateTime getLocalDateTime(Date date){
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}
	
	public Optional<ZonedDateTime> getLastUpdateAsZonedDateTime(Long lastUpdate){
		if (lastUpdate != null) {
			ZonedDateTime zonedDateTime =
				Instant.ofEpochMilli(lastUpdate).atZone(ZoneId.systemDefault());
			return Optional.of(zonedDateTime);
			
		}
		return Optional.empty();
	}
	
	public Optional<Date> getLastUpdateAsDate(Long lastUpdate){
		if (lastUpdate != null) {
			Date lastUpdateDate =
				Date.from(getLastUpdateAsZonedDateTime(lastUpdate).get().toInstant());
			return Optional.of(lastUpdateDate);
		}
		return Optional.empty();
	}
	
	public Reference getReference(String resourceType, Identifiable dbObject){
		return new Reference(new IdDt("Patient", dbObject.getId()));
	}
	
	public static void acquireAndReleaseLock(Identifiable dbObj){
		Optional<LockInfo> lr = LockServiceInstance.INSTANCE.acquireLockBlocking(dbObj, 5);
		if (lr.isPresent()) {
			LockResponse lrs = LockServiceInstance.INSTANCE.releaseLock(lr.get());
			if (!lrs.isOk()) {
				logger.warn("Could not release lock for [{}] [{}]", dbObj.getClass().getName(),
					dbObj.getId());
			}
		} else {
			logger.warn("Could not acquire lock for [{}] [{}]", dbObj.getClass().getName(),
				dbObj.getId());
		}
	}
	
	public static Optional<LockInfo> acquireLock(Identifiable dbObj){
		return LockServiceInstance.INSTANCE.acquireLockBlocking(dbObj, 5);
	}
	
	public static void releaseLock(LockInfo lockInfo){
		LockServiceInstance.INSTANCE.releaseLock(lockInfo);
	}
	
	public void setText(DomainResource domainResource, String text){
		Narrative narrative = domainResource.getText();
		if (narrative == null) {
			narrative = new Narrative();
		}
		String divEncodedText = text.replaceAll("<", "&lt;").replaceAll(">", "&gt;")
			.replaceAll("&", "&amp;").replaceAll("(\r\n|\r|\n)", "<br />");
		narrative.setDivAsString(divEncodedText);
		domainResource.setText(narrative);
	}
	
	public Optional<String> getText(DomainResource domainResource){
		Narrative narrative = domainResource.getText();
		if (narrative != null && narrative.getDivAsString() != null) {
			return ModelUtil.getNarrativeAsString(narrative);
		}
		return Optional.empty();
	}
}
