package es.fhir.rest.core.transformer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hl7.fhir.dstu3.model.Annotation;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.MedicationOrder;
import org.hl7.fhir.dstu3.model.MedicationOrder.MedicationOrderDosageInstructionComponent;
import org.hl7.fhir.dstu3.model.MedicationOrder.MedicationOrderEventHistoryComponent;
import org.hl7.fhir.dstu3.model.MedicationOrder.MedicationOrderStatus;
import org.hl7.fhir.dstu3.model.Narrative;
import org.hl7.fhir.dstu3.model.Reference;
import org.hl7.fhir.dstu3.model.StringType;
import org.hl7.fhir.dstu3.model.Type;
import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.primitive.IdDt;
import ch.elexis.core.model.prescription.Constants;
import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem_;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;
import info.elexis.server.core.connector.elexis.services.JPAQuery;
import info.elexis.server.core.connector.elexis.services.JPAQuery.QUERY;
import info.elexis.server.core.connector.elexis.services.PrescriptionService;

@Component
public class MedicationOrderPrescriptionTransformer implements IFhirTransformer<MedicationOrder, Prescription> {

	@Override
	public Optional<MedicationOrder> getFhirObject(Prescription localObject) {
		MedicationOrder order = new MedicationOrder();
		MedicationOrderStatus statusEnum = MedicationOrderStatus.ACTIVE;
		
		order.setId(new IdDt("MedicationOrder", localObject.getId()));
		
		Identifier elexisId = order.addIdentifier();
		elexisId.setSystem("www.elexis.info/objid");
		elexisId.setValue(localObject.getId());

		StringBuilder textBuilder = new StringBuilder();

		CodeableConcept medication = new CodeableConcept();
		String gtin = getArticleGtin(localObject);
		String atc = getArticleAtc(localObject);
		String articelLabel = getArticleLabel(localObject);
		if (gtin != null) {
			Coding coding = medication.addCoding();
			coding.setSystem("urn:oid:1.3.160‎");
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
		order.setMedication(medication);
		
		LocalDateTime dateFrom = localObject.getDateFrom();
		if (dateFrom != null) {
			Date time = Date.from(dateFrom.atZone(ZoneId.systemDefault()).toInstant());
			MedicationOrderEventHistoryComponent event = new MedicationOrderEventHistoryComponent();
			event.setDateTime(time);
			event.setStatus(MedicationOrderStatus.ACTIVE);
			order.addEventHistory(event);
		}
		LocalDateTime dateUntil = localObject.getDateUntil();
		if (dateUntil != null) {
			Date time = Date.from(dateFrom.atZone(ZoneId.systemDefault()).toInstant());
			MedicationOrderEventHistoryComponent event = new MedicationOrderEventHistoryComponent();
			event.setDateTime(time);
			event.setStatus(MedicationOrderStatus.STOPPED);

			String reasonText = localObject.getExtInfoAsString(Constants.FLD_EXT_STOP_REASON);
			if (reasonText != null && !reasonText.isEmpty()) {
				CodeableConcept reason = new CodeableConcept();
				reason.setText(reasonText);
				event.setReason(reason);
			}
			order.addEventHistory(event);
		}

		if (dateUntil != null) {
			if (dateUntil.isBefore(LocalDateTime.now()) || dateUntil.isEqual(dateFrom)) {
				statusEnum = MedicationOrderStatus.COMPLETED;
			}
		}

		String dose = localObject.getDosis();
		MedicationOrderDosageInstructionComponent dosage = null;
		if (dose != null && !dose.isEmpty()) {
			textBuilder.append(", ").append(dose);
			if (dosage == null) {
				dosage = order.addDosageInstruction();
			}
			dosage.setText(dose);
		}
		String disposalComment = localObject.getExtInfoAsString(Constants.FLD_EXT_DISPOSAL_COMMENT);
		if (disposalComment != null && !disposalComment.isEmpty()) {
			textBuilder.append(", ").append(disposalComment);
			if (dosage == null) {
				dosage = order.addDosageInstruction();
			}
			String dosageText = dosage.getText();
			if (dosageText != null && !dosageText.isEmpty()) {
				dosage.setText(dosage.getText() + ", " + disposalComment);
			} else {
				dosage.setText(disposalComment);
			}
		}
		String remark = localObject.getBemerkung();
		if (remark != null && !remark.isEmpty()) {
			textBuilder.append(", ").append(remark);
			order.addNote(new Annotation(new StringType(remark)));
		}

		order.setStatus(statusEnum);
		
		Narrative narrative = new Narrative();
		narrative.setDivAsString(textBuilder.toString());
		order.setText(narrative);
		return Optional.of(order);
	}

	@Override
	public Optional<Prescription> getLocalObject(MedicationOrder fhirObject) {
		String id = fhirObject.getId();
		if (id != null && !id.isEmpty()) {
			return PrescriptionService.INSTANCE.findById(id);
		}
		return Optional.empty();
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return MedicationOrder.class.equals(fhirClazz) && Prescription.class.equals(localClazz);
	}

	@Override
	public void updateLocalObject(MedicationOrder fhirObject, Prescription localObject) {
		Type medication = fhirObject.getMedication();
		if (medication instanceof CodeableConcept) {
			CodeableConcept medicationCode = (CodeableConcept) medication;
			for (Coding coding : medicationCode.getCoding()) {
				// update article according to gtin, if we can find it
				if ("urn:oid:1.3.160‎".equals(coding.getSystem())) {
					String gtin = coding.getCode();
					if (gtin != null && !gtin.isEmpty() && !gtin.equals(getArticleGtin(localObject))) {
						JPAQuery<ArtikelstammItem> qbe = new JPAQuery<ArtikelstammItem>(ArtikelstammItem.class);
						qbe.add(ArtikelstammItem_.gtin, QUERY.LIKE, gtin);
						Optional<ArtikelstammItem> updateAticle = qbe.executeGetSingleResult();
						updateAticle.ifPresent(ua -> localObject.setArtikel(ua));
					}
				}
			}
		} else if (medication instanceof Reference) {
			throw new IllegalStateException("Can not handle medication reference.");
		}

		List<MedicationOrderDosageInstructionComponent> dosageInstructions = fhirObject.getDosageInstruction();
		for (MedicationOrderDosageInstructionComponent medicationOrderDosageInstructionComponent : dosageInstructions) {

		}

		List<MedicationOrderEventHistoryComponent> history = fhirObject.getEventHistory();
		for (MedicationOrderEventHistoryComponent medicationOrderEventHistoryComponent : history) {

		}
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
	public Optional<Prescription> createLocalObject(MedicationOrder fhirObject) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}
}
