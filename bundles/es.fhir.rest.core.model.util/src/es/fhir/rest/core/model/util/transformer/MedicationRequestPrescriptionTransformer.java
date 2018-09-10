package es.fhir.rest.core.model.util.transformer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Dosage;
import org.hl7.fhir.dstu3.model.Enumeration;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.MedicationRequest;
import org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestDispenseRequestComponent;
import org.hl7.fhir.dstu3.model.MedicationRequest.MedicationRequestStatus;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.model.prescription.Constants;
import ch.elexis.core.model.prescription.EntryType;
import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.KontaktService;
import info.elexis.server.core.connector.elexis.services.PrescriptionService;

@Component
public class MedicationRequestPrescriptionTransformer implements IFhirTransformer<MedicationRequest, Prescription> {

	public static final String EXTENSION_PRESCRIPTION_ENTRYTYPE_URL = "www.elexis.info/extensions/prescription/entrytype";

	private PrescriptionEntryTypeFactory entryTypeFactory = new PrescriptionEntryTypeFactory();

	@Override
	public Optional<MedicationRequest> getFhirObject(Prescription localObject, Set<Include> includes) {
		MedicationRequest fhirObject = new MedicationRequest();
		MedicationRequestStatus statusEnum = MedicationRequestStatus.ACTIVE;

		fhirObject.setId(new IdDt("MedicationRequest", localObject.getId()));
		fhirObject.addIdentifier(getElexisObjectIdentifier(localObject));

		fhirObject.setSubject(getPatientReference(localObject.getPatient()));

		StringBuilder textBuilder = new StringBuilder();

		CodeableConcept medication = new CodeableConcept();
		String gtin = getArticleGtin(localObject);
		String atc = getArticleAtc(localObject);
		String articelLabel = getArticleLabel(localObject);
		if (gtin != null) {
			Coding coding = medication.addCoding();
			coding.setSystem("urn:oid:1.3.160");
			coding.setCode(gtin);
		}
		if (atc != null) {
			Coding coding = medication.addCoding();
			coding.setSystem("urn:oid:2.16.840.1.113883.6.73‎");
			coding.setCode(atc);
		}
		medication.setText(articelLabel);
		textBuilder.append(articelLabel);

		medication.setText(textBuilder.toString());
		fhirObject.setMedication(medication);

		MedicationRequestDispenseRequestComponent dispenseRequest = new MedicationRequestDispenseRequestComponent();
		Period dispensePeriod = new Period();
		LocalDateTime dateFrom = localObject.getDateFrom();
		if (dateFrom != null) {
			Date time = Date.from(dateFrom.atZone(ZoneId.systemDefault()).toInstant());
			dispensePeriod.setStart(time);
		}
		LocalDateTime dateUntil = localObject.getDateUntil();
		if (dateUntil != null) {
			Date time = Date.from(dateUntil.atZone(ZoneId.systemDefault()).toInstant());
			dispensePeriod.setEnd(time);

			String reasonText = localObject.getExtInfoAsString(Constants.FLD_EXT_STOP_REASON);
			if (reasonText != null && !reasonText.isEmpty()) {
				Annotation note = fhirObject.addNote();
				note.setText("Stop: " + reasonText);
			}
		}
		dispenseRequest.setValidityPeriod(dispensePeriod);
		fhirObject.setDispenseRequest(dispenseRequest);

		if (dateUntil != null) {
			if (dateUntil.isBefore(LocalDateTime.now()) || dateUntil.isEqual(dateFrom)) {
				statusEnum = MedicationRequestStatus.COMPLETED;
			}
		}

		String dose = localObject.getDosis();
		Dosage dosage = null;
		if (dose != null && !dose.isEmpty()) {
			textBuilder.append(", ").append(dose);
			if (dosage == null) {
				dosage = fhirObject.addDosageInstruction();
			}
			dosage.setText(dose);
		}
		String disposalComment = localObject.getExtInfoAsString(Constants.FLD_EXT_DISPOSAL_COMMENT);
		if (disposalComment != null && !disposalComment.isEmpty()) {
			textBuilder.append(", ").append(disposalComment);
			if (dosage == null) {
				dosage = fhirObject.addDosageInstruction();
			}
			CodeableConcept additional = dosage.addAdditionalInstruction();
			additional.setText(disposalComment);
		}
		String remark = localObject.getBemerkung();
		if (remark != null && !remark.isEmpty()) {
			textBuilder.append(", ").append(remark);
			fhirObject.addNote(new Annotation(new StringType(remark)));
		}

		fhirObject.setStatus(statusEnum);

		Narrative narrative = new Narrative();
		narrative.setDivAsString(textBuilder.toString());
		fhirObject.setText(narrative);

		Extension elexisEntryType = new Extension();
		elexisEntryType.setUrl(EXTENSION_PRESCRIPTION_ENTRYTYPE_URL);
		elexisEntryType
				.setValue(new Enumeration<>(entryTypeFactory, EntryType.byNumeric(getNumericEntryType(localObject))));
		fhirObject.addExtension(elexisEntryType);
		return Optional.of(fhirObject);
	}

	private int getNumericEntryType(Prescription localObject) {
		String prescriptionType = localObject.getPrescriptionType();
		if (prescriptionType != null && !prescriptionType.isEmpty()) {
			return Integer.parseInt(prescriptionType);
		}
		return -1;
	}

	private Reference getPatientReference(Kontakt patient) {
		Reference ref = new Reference();
		ref.setId(patient.getId());
		return ref;
	}

	@Override
	public Optional<Prescription> getLocalObject(MedicationRequest fhirObject) {
		String id = fhirObject.getIdElement().getIdPart();
		if (id != null && !id.isEmpty()) {
			return PrescriptionService.load(id);
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return MedicationRequest.class.equals(fhirClazz) && Prescription.class.equals(localClazz);
	}

	@Override
	public Optional<Prescription> updateLocalObject(MedicationRequest fhirObject, Prescription localObject) {
		Optional<MedicationRequest> localFhirObject = getFhirObject(localObject);
		if (!fhirObject.equalsDeep(localFhirObject.get())) {
			// a change means we need to stop the current prescription
			localObject.setDateUntil(LocalDateTime.now());
			localObject.setExtInfoValue(Constants.FLD_EXT_STOP_REASON, "Geändert durch FHIR Server");
			PrescriptionService.save(localObject);
			// and create a new one with the changed properties
			return createLocalObject(fhirObject);
		}
		return Optional.empty();
	}

	private String getArticleGtin(Prescription localObject) {
		AbstractDBObjectIdDeleted localArticel = localObject.getArtikel();
		if (localArticel instanceof ArtikelstammItem) {
			return ((ArtikelstammItem) localArticel).getGtin();
		} else if (localArticel instanceof Artikel) {
			return ((Artikel) localArticel).getEan();
		}
		return null;
	}

	private String getArticleAtc(Prescription localObject) {
		AbstractDBObjectIdDeleted localArticel = localObject.getArtikel();
		if (localArticel instanceof ArtikelstammItem) {
			return ((ArtikelstammItem) localArticel).getAtc();
		}
		return null;
	}

	private String getArticleLabel(Prescription localObject) {
		AbstractDBObjectIdDeleted localArticel = localObject.getArtikel();
		if (localArticel instanceof ArtikelstammItem) {
			return ((ArtikelstammItem) localArticel).getLabel();
		} else if (localArticel instanceof Artikel) {
			return ((Artikel) localArticel).getLabel();
		}
		return "Unknown article";
	}

	@Override
	public Optional<Prescription> createLocalObject(MedicationRequest fhirObject) {
		Optional<ArtikelstammItem> item = Optional.empty();
		Optional<String> gtin = getMedicationRequestGtin(fhirObject);
		if (gtin.isPresent()) {
			// lookup item
			JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
			qbe.add(ArtikelstammItem_.gtin, QUERY.EQUALS, gtin.get());
			item = qbe.executeGetSingleResult();
		} else {
			LoggerFactory.getLogger(getClass()).error("MedicationOrder with no gtin");
		}
		// lookup patient
		Optional<Kontakt> patient = KontaktService.load(fhirObject.getSubject().getId());
		if (item.isPresent() && patient.isPresent()) {
			Prescription localObject = new PrescriptionService.Builder(item.get(), patient.get(),
					getMedicationRequestDosage(fhirObject)).build();

			Optional<LocalDateTime> startDateTime = getMedicationRequestStartDateTime(fhirObject);
			startDateTime.ifPresent(date -> localObject.setDateFrom(date));

			Optional<LocalDateTime> endDateTime = getMedicationRequestEndDateTime(fhirObject);
			endDateTime.ifPresent(date -> localObject.setDateFrom(date));

			localObject.setExtInfoValue(Constants.FLD_EXT_DISPOSAL_COMMENT,
					getMedicationRequestAdditionalInstructions(fhirObject));

			localObject.setBemerkung(getMedicationRequestRemark(fhirObject));

			Optional<String> prescriptionType = getMedicationRequestPrescriptionType(fhirObject);
			prescriptionType.ifPresent(p -> localObject.setPrescriptionType(p));

			return Optional.of((Prescription) PrescriptionService.save(localObject));
		}
		return Optional.empty();
	}

	private Optional<String> getMedicationRequestPrescriptionType(MedicationRequest fhirObject) {
		List<Extension> extensionsEntryType = fhirObject.getExtensionsByUrl(EXTENSION_PRESCRIPTION_ENTRYTYPE_URL);
		for (Extension extension : extensionsEntryType) {
			try {
				EntryType entryType = EntryType.valueOf(((CodeType) extension.getValue()).getValue());
				return Optional.of(Integer.toString(entryType.numericValue()));
			} catch (IllegalArgumentException iae) {
			}
		}
		return Optional.empty();
	}

	private Optional<LocalDateTime> getMedicationRequestEndDateTime(MedicationRequest fhirObject) {
		MedicationRequestDispenseRequestComponent dispenseRequest = fhirObject.getDispenseRequest();
		if (dispenseRequest != null) {
			Period period = dispenseRequest.getValidityPeriod();
			if (period != null && period.hasEnd()) {
				Date endDate = period.getEnd();
				return Optional.of(LocalDateTime.ofInstant(endDate.toInstant(), ZoneId.systemDefault()));
			}
		}
		return Optional.empty();
	}

	private Optional<LocalDateTime> getMedicationRequestStartDateTime(MedicationRequest fhirObject) {
		MedicationRequestDispenseRequestComponent dispenseRequest = fhirObject.getDispenseRequest();
		if (dispenseRequest != null) {
			Period period = dispenseRequest.getValidityPeriod();
			if (period != null && period.hasStart()) {
				Date startDate = period.getStart();
				return Optional.of(LocalDateTime.ofInstant(startDate.toInstant(), ZoneId.systemDefault()));
			}
		}
		return Optional.empty();
	}

	private String getMedicationRequestRemark(MedicationRequest fhirObject) {
		List<Annotation> notes = fhirObject.getNote();
		StringBuilder sb = new StringBuilder();
		for (Annotation annotation : notes) {
			String text = annotation.getText();
			if (text != null) {
				if (sb.length() == 0) {
					sb.append(text);
				} else {
					sb.append(", ").append(text);
				}
			}
		}
		return sb.toString();
	}

	private Object getMedicationRequestAdditionalInstructions(MedicationRequest fhirObject) {
		List<Dosage> instructions = fhirObject.getDosageInstruction();
		StringBuilder sb = new StringBuilder();
		for (Dosage dosage : instructions) {
			List<CodeableConcept> additionals = dosage.getAdditionalInstruction();
			for (CodeableConcept codeableConcept : additionals) {
				String text = codeableConcept.getText();
				if (text != null) {
					if (sb.length() == 0) {
						sb.append(text);
					} else {
						sb.append(", ").append(text);
					}
				}
			}
		}
		return sb.toString();
	}

	private Optional<String> getMedicationRequestGtin(MedicationRequest fhirObject) {
		Type medication = fhirObject.getMedication();
		if (medication instanceof CodeableConcept) {
			List<Coding> codings = ((CodeableConcept) medication).getCoding();
			for (Coding coding : codings) {
				String codeSystem = coding.getSystem();
				if ("urn:oid:1.3.160".equals(codeSystem)) {
					return Optional.of(coding.getCode());
				}
			}
		}
		return Optional.empty();
	}

	private String getMedicationRequestDosage(MedicationRequest fhirObject) {
		List<Dosage> instructions = fhirObject.getDosageInstruction();
		StringBuilder sb = new StringBuilder();
		for (Dosage dosage : instructions) {
			String text = dosage.getText();
			if (text != null) {
				if (sb.length() == 0) {
					sb.append(text);
				} else {
					sb.append(", ").append(text);
				}
			}
		}
		return sb.toString();
	}
}
