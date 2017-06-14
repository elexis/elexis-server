package es.fhir.rest.core.model.util.transformer.helper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Encounter;
import org.hl7.fhir.dstu3.model.Encounter.EncounterParticipantComponent;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.Reference;

import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.model.FallConstants;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Behandlung;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Fall;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.services.BehandlungService;
import info.elexis.server.core.connector.elexis.services.FallService;
import info.elexis.server.core.connector.elexis.services.KontaktService;

public class BehandlungHelper extends AbstractHelper {

	public static Optional<Behandlung> createBehandlung(IEncounter iEncounter) {
		Optional<Behandlung> ret = getBehandlung(iEncounter);
		if (!ret.isPresent()) {
			Optional<Kontakt> patient = getPatient(iEncounter);
			Optional<Kontakt> serviceProvider = getPerformer(iEncounter);
			if (patient.isPresent() && serviceProvider.isPresent()) {
				Fall fall = getOrCreateDefaultFall(patient.get());
				Behandlung behandlung = new BehandlungService.Builder(fall, serviceProvider.get()).buildAndSave();
				Optional<LocalDateTime> startTime = iEncounter.getStartTime();
				if (startTime.isPresent()) {
					behandlung.setDatum(startTime.get().toLocalDate());
				} else {
					behandlung.setDatum(LocalDate.now());
				}
				behandlung = (Behandlung) BehandlungService.save(behandlung);
				ret = Optional.of(behandlung);
			}
		}
		return ret;
	}

	private static Fall getOrCreateDefaultFall(Kontakt kontakt) {
		List<Fall> faelle = KontaktService.getFaelle(kontakt);
		Fall defaultFall = lookUpDefaultFall(faelle);
		if (defaultFall == null) {
			defaultFall = createDefaultFall(kontakt);
			acquireAndReleaseLock(defaultFall);
		}
		return defaultFall;
	}

	private static Fall createDefaultFall(Kontakt kontakt) {
		Fall ret = new FallService.Builder(kontakt, "online", FallConstants.TYPE_DISEASE, "KVG").buildAndSave();
		closeFall(ret);
		return ret;
	}

	private static void closeFall(Fall fall) {
		Optional<LockInfo> lockInfo = AbstractHelper.acquireLock(fall);
		if (lockInfo.isPresent()) {
			fall.setDatumBis(fall.getDatumVon());
			FallService.save(fall);
			AbstractHelper.releaseLock(lockInfo.get());
		}
	}

	private static Fall lookUpDefaultFall(List<Fall> faelle) {
		Fall ret = null;
		if (faelle != null) {
			for (Fall fall : faelle) {
				if (fall.getBezeichnung().equals("online")) {
					// is the only, or the newest online fall
					if (ret == null) {
						ret = fall;
					} else if (ret != null && (fall.getLastupdate().compareTo(ret.getLastupdate()) == 1)) {
						ret = fall;
					}
				}
			}
		}
		if (ret != null) {
			// make sure default fall is closed
			if (ret.getDatumBis() == null) {
				closeFall(ret);
			}
		}
		return ret;
	}

	private static Optional<Behandlung> getBehandlung(IEncounter iEncounter) {
		String behandlungsId = iEncounter.getConsultationId();
		if (behandlungsId != null && !behandlungsId.isEmpty()) {
			return BehandlungService.load(behandlungsId);
		}
		return Optional.empty();
	}

	public static Optional<Kontakt> getPerformer(IEncounter iEncounter) {
		return KontaktService.load(iEncounter.getMandatorId());
	}

	public static Optional<Kontakt> getPatient(IEncounter iEncounter) {
		return KontaktService.load(iEncounter.getPatientId());
	}

	public static Optional<String> getMandatorId(Encounter fhirObject) {
		List<EncounterParticipantComponent> participants = fhirObject.getParticipant();
		for (EncounterParticipantComponent encounterParticipantComponent : participants) {
			if (encounterParticipantComponent.hasIndividual()) {
				Reference reference = encounterParticipantComponent.getIndividual();
				if (reference.getReferenceElement().getResourceType().equals(Practitioner.class.getSimpleName())) {
					return Optional.of(reference.getReferenceElement().getIdPart());
				}
			}
		}
		return Optional.empty();
	}

	public static Optional<String> getPatientId(Encounter fhirObject) {
		if (fhirObject.getSubject() != null && fhirObject.getSubject().hasReference()) {
			return Optional.of(fhirObject.getSubject().getReferenceElement().getIdPart());
		}
		return Optional.empty();
	}
}
