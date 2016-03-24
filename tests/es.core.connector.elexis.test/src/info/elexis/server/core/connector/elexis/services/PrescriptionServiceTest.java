package info.elexis.server.core.connector.elexis.services;

import static org.junit.Assert.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.types.Gender;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.ArtikelstammItem;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Prescription;

public class PrescriptionServiceTest {

	private Kontakt patient;
	private ArtikelstammItem article;
	private ArtikelstammItem product;

	@Before
	public void before() {
		patient = KontaktService.INSTANCE.createPatient("Vorname", "Nachname", LocalDate.now(), Gender.UNDEFINED);
		article = ArtikelstammItemService.INSTANCE.create("0768056318007949855760001", true);
		product = ArtikelstammItemService.INSTANCE.create("563182", true);
		assertNotNull(article);
		assertNotNull(product);
	}

	@After
	public void after() {
		KontaktService.INSTANCE.remove(patient);
		ArtikelstammItemService.INSTANCE.remove(article);
		ArtikelstammItemService.INSTANCE.remove(product);
	}

	@Test
	public void testAddAndRemovePrescription() {
		Prescription articlePres = PrescriptionService.INSTANCE.create(article, patient, "1-1-0-0");
		Prescription productPres = PrescriptionService.INSTANCE.create(product, patient, "1-1-0-0");
		Prescription deletedPres = PrescriptionService.INSTANCE.create(article, patient, "1-1-2-1");
		deletedPres.setDeleted(true);
		Prescription recipePres = PrescriptionService.INSTANCE.create(article, patient, "1-1-2-1");
		recipePres.setRezeptID("nonExistRecipeId");
		PrescriptionService.INSTANCE.flush();

		assertNotNull(articlePres.getDateFrom());
		assertNotNull(productPres.getDateFrom());

		List<Prescription> prescList = PrescriptionService.findAllNonDeletedPrescriptionsForPatient(patient);
		assertEquals(2, prescList.size());

		PrescriptionService.INSTANCE.remove(articlePres);
		PrescriptionService.INSTANCE.remove(productPres);
		PrescriptionService.INSTANCE.remove(deletedPres);
		PrescriptionService.INSTANCE.remove(recipePres);
	}

}
