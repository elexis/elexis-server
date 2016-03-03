package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.elexis.server.core.connector.elexis.jpa.model.annotated.AbstractDBObjectIdDeleted;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;

public class PrescriptionServiceTest {

	private Kontakt patient;
	private AbstractDBObjectIdDeleted article;
	private AbstractDBObjectIdDeleted product;

	@Before
	public void setupPatientAndBehandlung() {
		patient = KontaktService.INSTANCE.createPatient();
		article = ArtikelstammItemService.INSTANCE.findById("0768056318007949855760001");
		product = ArtikelstammItemService.INSTANCE.findById("563182");
		assertNotNull(article);
		assertNotNull(product);
	}

	@After
	public void teardownPatientAndBehandlung() {
		KontaktService.INSTANCE.remove(patient);
	}

	@Test
	public void testAddAndRemovePrescription() {
		Prescription articlePres = PrescriptionService.INSTANCE.create(article, patient, "1-1-0-0");
		Prescription productPres = PrescriptionService.INSTANCE.create(product, patient, "1-1-0-0");
		
		assertNotNull(articlePres.getDateFrom());
		assertNotNull(productPres.getDateFrom());
		
		List<Prescription> prescList = PrescriptionService.findAllNonDeletedPrescriptionsForPatient(patient);
		assertEquals(2, prescList.size());
		
		PrescriptionService.INSTANCE.remove(articlePres);
		PrescriptionService.INSTANCE.remove(productPres);
	}

}
