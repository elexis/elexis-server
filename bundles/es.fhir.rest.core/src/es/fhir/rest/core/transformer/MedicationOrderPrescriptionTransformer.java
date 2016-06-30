package es.fhir.rest.core.transformer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.osgi.service.component.annotations.Component;

import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.NarrativeDt;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder;
import ca.uhn.fhir.model.dstu2.resource.MedicationOrder.DosageInstruction;
import ca.uhn.fhir.model.dstu2.valueset.MedicationOrderStatusEnum;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.UriDt;
import es.fhir.rest.core.IFhirTransformer;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Artikel;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;

@Component
public class MedicationOrderPrescriptionTransformer implements IFhirTransformer<MedicationOrder, Prescription> {

	@Override
	public MedicationOrder getFhirObject(Prescription localObject) {
		MedicationOrder order = new MedicationOrder();
		MedicationOrderStatusEnum statusEnum = MedicationOrderStatusEnum.ACTIVE;
		
		order.setId(new IdDt("MedicationOrder", localObject.getId()));
		
		IdentifierDt elexisId = order.addIdentifier();
		elexisId.setSystem(new UriDt("www.elexis.info/objid"));
		elexisId.setValue(localObject.getId());

		StringBuilder textBuilder = new StringBuilder();

		CodeableConceptDt medication = new CodeableConceptDt();
		if (localObject.getArtikel() instanceof ArtikelstammItem) {
			ArtikelstammItem article = (ArtikelstammItem) localObject.getArtikel();
			String gtin = article.getGtin();
			if (gtin != null) {
				CodingDt coding = medication.addCoding();
				coding.setSystem("urn:oid:1.3.160‎");
				coding.setCode(gtin);
			}
			String atc = article.getAtc();
			if (atc != null) {
				CodingDt coding = medication.addCoding();
				coding.setSystem("urn:oid:2.16.840.1.113883.6.73‎");
				coding.setCode(atc);
			}		
			medication.setText(article.getLabel());
			textBuilder.append(article.getLabel());
		} else if (localObject.getArtikel() instanceof Artikel) {
			Artikel article = (Artikel) localObject.getArtikel();
			String gtin = article.getEan();
			if (gtin != null) {
				CodingDt coding = medication.addCoding();
				coding.setSystem("urn:oid:1.3.160‎");
				coding.setCode(gtin);
			}
			medication.setText(article.getLabel());
			textBuilder.append(article.getLabel());
		} else {
			medication.setText("Unknown article");
			textBuilder.append("Unknown article");
		}

		medication.setText(textBuilder.toString());
		order.setMedication(medication);
		
		LocalDateTime dateFrom = localObject.getDateFrom();
		if (dateFrom != null) {
			DateTimeDt time = new DateTimeDt(Date.from(dateFrom.atZone(ZoneId.systemDefault()).toInstant()));
			order.setDateWritten(time);
		}
		LocalDateTime dateUntil = localObject.getDateUntil();
		if (dateUntil != null) {
			DateTimeDt time = new DateTimeDt(Date.from(dateFrom.atZone(ZoneId.systemDefault()).toInstant()));
			order.setDateEnded(time);
		}

		if (dateUntil != null) {
			if (dateUntil.isBefore(LocalDateTime.now()) || dateUntil.isEqual(dateFrom)) {
				statusEnum = MedicationOrderStatusEnum.COMPLETED;
			}
		}

		String dose = localObject.getDosis();
		DosageInstruction dosage = null;
		if (dose != null && !dose.isEmpty()) {
			textBuilder.append(", ").append(dose);
			if (dosage == null) {
				dosage = order.addDosageInstruction();
			}
			dosage.setText(dose);
		}
		String disposalComment = localObject.getExtInfoAsString("disposalComment");
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
			order.setNote(remark);
		}

		order.setStatus(statusEnum);
		
		NarrativeDt narrative = new NarrativeDt();
		narrative.setDiv(textBuilder.toString());
		order.setText(narrative);
		return order;
	}

	@Override
	public Prescription getLocalObject(MedicationOrder fhirObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz) {
		return MedicationOrder.class.equals(fhirClazz) && Prescription.class.equals(localClazz);
	}
}
